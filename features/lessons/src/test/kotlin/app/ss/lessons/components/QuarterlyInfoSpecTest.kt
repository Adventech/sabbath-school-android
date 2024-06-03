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

package app.ss.lessons.components

import app.ss.lessons.components.spec.findIndex
import app.ss.models.SSLesson
import org.amshove.kluent.shouldBeEqualTo
import org.joda.time.DateTime
import org.junit.Test

class QuarterlyInfoSpecTest {

    @Test
    fun `should find lesson index on Wednesday`() {
        val index = findIndex(
            lessons = buildLessons(),
            today = DateTime.parse("2022-08-17T13:30:05.000Z")
        )

        // Wednesday 17th August 2022 would fall under the 8th lesson of the quarter
        index shouldBeEqualTo "lesson_8"
    }

    @Test
    fun `should use previous week lesson index on Sabbath morning`() {
        val index = findIndex(
            lessons = buildLessons(),
            today = DateTime.parse("2022-08-20T09:30:05.000Z")
        )

        // Sabbath morning 20th August 2022 would fall back to the 8th lesson of the quarter
        index shouldBeEqualTo "lesson_8"
    }

    @Test
    fun `should use current week lesson index on Sabbath afternoon`() {
        val index = findIndex(
            lessons = buildLessons(),
            today = DateTime.parse("2022-08-20T15:40:05.000Z")
        )

        // Sabbath afternoon 20th August 2022 would fall under the 9th lesson of the quarter
        index shouldBeEqualTo "lesson_9"
    }

    /**
     * Builds 13 [SSLesson]s starting from Sunday 25 June 2022.
     * 13 is the average number of lessons in a quarter.
     */
    private fun buildLessons(): List<SSLesson> {
        val lessons = mutableListOf<SSLesson>()

        val weekStart = DateTime.parse("2022-06-25T13:30:05.000Z")
        var currentWeek = weekStart

        for (i in 1..13) {
            val firstDay = currentWeek
            val lastDay = currentWeek.plusDays(6)
            lessons.add(
                SSLesson(
                    title = "Lesson $i",
                    start_date = "${firstDay.dayOfMonth().get()}/${firstDay.monthOfYear().get()}/${firstDay.year().get()}",
                    end_date = "${lastDay.dayOfMonth().get()}/${lastDay.monthOfYear().get()}/${lastDay.year().get()}",
                    index = "lesson_$i"
                )
            )

            currentWeek = currentWeek.plusWeeks(1)
        }

        return lessons
    }
}
