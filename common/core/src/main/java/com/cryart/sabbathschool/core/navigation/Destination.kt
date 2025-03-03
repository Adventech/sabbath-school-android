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

package com.cryart.sabbathschool.core.navigation

import android.net.Uri
import androidx.core.net.toUri

enum class Destination(val key: String) {

    ABOUT("about"),
    READ_WEB("sabbath-school.adventech.io"),
    ;

    companion object {
        private val map = entries.associateBy(Destination::key)

        fun fromKey(type: String) = map[type]
    }
}

private const val SCHEME = "ss_app://"

/**
 * Builds a navigation [Uri] from a [Destination]
 *
 * Example:
 * Passing [Destination.READ] with extras [Pair("lesson_index", "index")]
 * will return `ss_app://read?lesson_index=index`
 *
 */
fun Destination.toUri(vararg extras: Pair<String, String> = emptyArray()): Uri {
    val query = if (extras.isNotEmpty()) {
        extras.joinToString(separator = "&", prefix = "?") { pair ->
            "${pair.first}=${pair.second}"
        }
    } else {
        ""
    }
    return "$SCHEME$key$query".toUri()
}
