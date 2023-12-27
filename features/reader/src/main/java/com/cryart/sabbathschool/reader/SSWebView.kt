/*
 * Copyright (c) 2023. Adventech <info@adventech.io>
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
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.cryart.sabbathschool.reader

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.webkit.WebView
import android.webkit.WebViewClient
import com.cryart.sabbathschool.core.extensions.context.isDarkTheme
import ss.misc.SSConstants
import ss.prefs.model.SSReadingDisplayOptions
import ss.prefs.model.displayTheme
import timber.log.Timber
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

@SuppressLint("SetJavaScriptEnabled")
open class SSWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    init {
        if (!isInEditMode) {
            this.webViewClient = WebViewClient()
            this.settings.javaScriptEnabled = true
            this.settings.allowFileAccess = true
        }
    }

    open fun loadContent(contentData: String, ssReadingDisplayOptions: SSReadingDisplayOptions) {
        var baseUrl = SSConstants.SS_READER_APP_BASE_URL

        val indexFile = File(context.filesDir.toString() + "/index.html")
        var content = if (indexFile.exists()) {
            baseUrl = "file:///" + context.filesDir + "/"
            readFileFromFiles(context.filesDir.toString() + "/index.html")
        } else {
            readFileFromAssets(context)
        }
        try {
            val parsedData = contentData.replace("$", "\\$")
            content = content.replace(Regex.fromLiteral("{{content}}"), parsedData)
            content = content.replace("ss-wrapper-light", "ss-wrapper-${ssReadingDisplayOptions.displayTheme(context.isDarkTheme())}")
            content = content.replace("ss-wrapper-andada", "ss-wrapper-${ssReadingDisplayOptions.font}")
            content = content.replace("ss-wrapper-medium", "ss-wrapper-${ssReadingDisplayOptions.size}")
            loadDataWithBaseURL(baseUrl, content, "text/html", "utf-8", null)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun readFileFromFiles(path: String): String {
        val file = File(path)
        return try {
            val length = file.length().toInt()
            val bytes = ByteArray(length)
            FileInputStream(file).use { `in` -> `in`.read(bytes) }
            String(bytes)
        } catch (e: Exception) {
            Timber.e(e)
            ""
        }
    }

    private fun readFileFromAssets(context: Context): String {
        val buf = StringBuilder()
        return try {
            val json = context.assets.open(SSConstants.SS_READER_APP_ENTRYPOINT)
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
}
