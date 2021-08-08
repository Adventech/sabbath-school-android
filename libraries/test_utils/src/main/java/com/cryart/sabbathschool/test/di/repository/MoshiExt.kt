/*
 * Copyright (c) 2021. Adventech <info@adventech.io>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.cryart.sabbathschool.test.di.repository

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.RawRes
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import timber.log.Timber
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.Reader
import java.io.StringWriter
import java.lang.reflect.Type
import java.nio.charset.StandardCharsets

internal inline fun <reified T> Moshi.fromRawRes(
    context: Context,
    filePath: String
): List<T> {
    val json = context.jsonFromAssets(filePath)
    val listDataType: Type = Types.newParameterizedType(List::class.java, T::class.java)
    val adapter: JsonAdapter<List<T>> = adapter(listDataType)
    return adapter.fromJson(json) ?: emptyList()
}

@SuppressLint("LongLogTag")
internal fun Context.getJson(@RawRes resId: Int): String {
    val resourceReader = resources.openRawResource(resId)
    val writer = StringWriter()

    try {
        val reader = BufferedReader(InputStreamReader(resourceReader, "UTF-8") as Reader)
        var line: String? = reader.readLine()
        while (line != null) {
            writer.write(line)
            line = reader.readLine()
        }
        return writer.toString()
    } catch (e: Exception) {
        Timber.e(e)
    } finally {
        try {
            resourceReader.close()
        } catch (e: Exception) {
            Timber.e(e, "Unhandled exception while using JSONResourceReader")
        }
    }
    return ""
}

internal fun Context.jsonFromAssets(assetPath: String): String {
    val buf = StringBuilder()
    return try {
        val json = assets.open(assetPath)
        val `in` = BufferedReader(InputStreamReader(json, StandardCharsets.UTF_8))
        var str: String?
        while (`in`.readLine().also { str = it } != null) {
            buf.append(str)
        }
        `in`.close()
        buf.toString()
    } catch (e: IOException) {
        Timber.e(e)
        ""
    }
}
