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
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package ss.misc

import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import timber.log.Timber
import java.util.Locale

object DateHelper {

    fun parseDate(date: String): DateTime? = try {
        DateTimeFormat.forPattern(SSConstants.SS_DATE_FORMAT)
            .parseLocalDate(date).toDateTimeAtStartOfDay()
    } catch (ex: Exception) {
        Timber.e(ex)
        null
    }

    fun formatDate(date: String, format: String = SSConstants.SS_DATE_FORMAT_OUTPUT_DAY): String {
        return try {
            DateTimeFormat.forPattern(format)
                .withLocale(Locale.getDefault())
                .print(
                    DateTimeFormat.forPattern(SSConstants.SS_DATE_FORMAT)
                        .parseLocalDate(date)
                ).replace("Saturday", "Sabbath")
                .replaceFirstChar { it.uppercase() }
        } catch (ex: IllegalArgumentException) {
            Timber.e(ex)
            return ""
        }
    }

    fun today() = formatDate(LocalDate.now().toString(SSConstants.SS_DATE_FORMAT))
}
