/*
 * Copyright (c) 2022. Adventech <info@adventech.io>
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
package app.ss.models

import androidx.annotation.Keep

@Keep
data class SSRead(
    val id: String = "",
    val date: String = "",
    val index: String = "",
    val title: String = "",
    val content: String = "",
    val bible: List<SSBibleVerses> = emptyList()
) {

    /**
     * Convert a Read index of
     * "en-2021-03-04-02" to "en/2021-03/04/02"
     *
     * Or
     * "en-2021-03-04-teacher-comments" to "en/2021-03/04/09-teacher-comments"
     *
     * @param lessonShareIndex : Should already be formatted from [SSLessonInfo.shareIndex]
     * @param day : Day 1 - 9
     */
    fun shareIndex(lessonShareIndex: String, day: Int): String {
        val dayStr = "$day".padStart(2, '0')
        val readPath = if (day > 7) {
            "$dayStr-${title.replace(' ', '-').lowercase()}"
        } else {
            dayStr
        }
        return "$lessonShareIndex/$readPath"
    }
}
