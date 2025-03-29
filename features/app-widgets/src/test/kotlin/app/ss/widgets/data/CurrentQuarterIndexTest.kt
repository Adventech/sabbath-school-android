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

import org.amshove.kluent.shouldBeEqualTo
import org.joda.time.LocalDate
import org.junit.Test
import java.util.Calendar

class CurrentQuarterIndexTest {

    @Test
    fun `generate index - Jan 4`() {
        val today = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2025)
            set(Calendar.MONTH, Calendar.JANUARY)
            set(Calendar.DAY_OF_MONTH, 4)
        }
        val currentDate = LocalDate.fromCalendarFields(today)

        val index = CurrentQuarterIndex(currentDate, "en")

        index shouldBeEqualTo "en-2025-01"
    }

    @Test
    fun `generate index - March 29`() {
        val today = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2025)
            set(Calendar.MONTH, Calendar.MARCH)
            set(Calendar.DAY_OF_MONTH, 29)
        }
        val currentDate = LocalDate.fromCalendarFields(today)

        val index = CurrentQuarterIndex(currentDate, "en")

        index shouldBeEqualTo "en-2025-02"
    }

    @Test
    fun `generate index - June 21`() {
        val today = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2025)
            set(Calendar.MONTH, Calendar.JUNE)
            set(Calendar.DAY_OF_MONTH, 21)
        }
        val currentDate = LocalDate.fromCalendarFields(today)

        val index = CurrentQuarterIndex(currentDate, "en")

        index shouldBeEqualTo "en-2025-03"
    }

    @Test
    fun `generate index - Sep 13`() {
        val today = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2025)
            set(Calendar.MONTH, Calendar.SEPTEMBER)
            set(Calendar.DAY_OF_MONTH, 13)
        }
        val currentDate = LocalDate.fromCalendarFields(today)

        val index = CurrentQuarterIndex(currentDate, "en")

        index shouldBeEqualTo "en-2025-04"
    }

    @Test
    fun `generate index - Dec 31`() {
        val today = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2025)
            set(Calendar.MONTH, Calendar.DECEMBER)
            set(Calendar.DAY_OF_MONTH, 31)
        }
        val currentDate = LocalDate.fromCalendarFields(today)

        val index = CurrentQuarterIndex(currentDate, "en")

        index shouldBeEqualTo "en-2025-04"
    }
}
