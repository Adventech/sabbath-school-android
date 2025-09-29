/*
 * Copyright (c) 2025. Adventech <info@adventech.io>
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

package ss.lessons.impl.repository

import org.joda.time.DateTimeConstants
import org.joda.time.LocalDate

object CurrentQuarterIndex {

    /**
     * Generates a quarter index based on the current date and language code.
     *
     * @param currentDate The current date.
     * @param languageCode The language code (e.g., "en").
     * @return A string representing the quarter index in the format "languageCode-year-quarter".
     */
    operator fun invoke(currentDate: LocalDate, languageCode: String): String {
        var year = currentDate.year

        val q1 = firstSaturdayOnOrAfter(LocalDate(year, 1, 1))
        val q2 = firstSaturdayOnOrAfter(LocalDate(year, 4, 1))
        val q3 = firstSaturdayOnOrAfter(LocalDate(year, 7, 1))
        val q4 = firstSaturdayOnOrAfter(LocalDate(year, 10, 1))

        val quarter = when {
            currentDate < q1 -> {
                // We're in the tail of last year's Q4
                year -= 1
                "04"
            }
            currentDate < q2 -> "01"
            currentDate < q3 -> "02"
            currentDate < q4 -> "03"
            else -> "04"
        }

        return "$languageCode-$year-$quarter"
    }

    private fun firstSaturdayOnOrAfter(start: LocalDate): LocalDate {
        var date = start
        while (date.dayOfWeek != DateTimeConstants.SATURDAY) {
            date = date.plusDays(1)
        }
        return date
    }
}
