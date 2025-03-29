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

package app.ss.widgets.data

import org.joda.time.DateTimeConstants
import org.joda.time.Days
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
        val year = currentDate.year
        val firstSaturday = getFirstSaturdayOfYear(year)

        // Calculate exact week number (Sat-Fri weeks)
        val daysBetween = Days.daysBetween(firstSaturday, currentDate).days
        val weekNumber = if (daysBetween >= 0) (daysBetween / 7) + 1 else 1

        // Quarter mapping (12 weeks per quarter)
        val quarter = when (weekNumber) {
            in 1..12 -> "01"   // Q1 (Weeks 1-12)
            in 13..24 -> "02"  // Q2 (Weeks 13-24)
            in 25..36 -> "03"  // Q3 (Weeks 25-36)
            else -> "04"       // Q4 (Weeks 37+)
        }
        return "$languageCode-$year-$quarter"
    }

    private fun getFirstSaturdayOfYear(year: Int): LocalDate {
        var date = LocalDate(year, 1, 1)
        while (date.dayOfWeek != DateTimeConstants.SATURDAY) {
            date = date.plusDays(1)
        }
        return date
    }
}
