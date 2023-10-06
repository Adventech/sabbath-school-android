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

package com.cryart.sabbathschool.test.di.mock

import app.ss.models.SSBibleVerses
import app.ss.models.SSDay
import app.ss.models.SSLesson
import app.ss.models.SSLessonInfo
import app.ss.models.SSRead
import app.ss.models.TodayData
import app.ss.models.WeekData
import app.ss.models.WeekDay

/**
 * Mock data for UI tests
 */
object MockDataFactory {

    val versions = listOf(
        "NASB",
        "NKJV",
        "KJV"
    )
    private val verses = mapOf(
        "1John14" to "<h2>1 John 1:4</h2><sup>4</sup> These things we write, so that our joy may be made complete."
    )

    val bibleVerses: List<SSBibleVerses> = versions.map { version ->
        SSBibleVerses(
            name = version,
            verses = verses
        )
    }

    fun lessonInfo(): SSLessonInfo = SSLessonInfo(
        SSLesson("Lesson", cover = "cover_url"),
        listOf(SSDay(title = "Day Title", date = "03/04/2021"))
    )

    fun ssRead(
        id: String = "",
        date: String = "20/06/2021",
        index: String = "en-2021-02-13-02",
        title: String = "Joy"
    ): SSRead = SSRead(
        id,
        bible = bibleVerses,
        date = date,
        index = index,
        title = title,
        content = "Look at what John wrote here. In a few simple words"
    )

    fun todayModel(): TodayData = TodayData(
        "index",
        "lessonIndex",
        "Worn and Weary",
        "Sunday, June 27",
        "cover",
        "",
        null
    )

    fun weekData(): WeekData = WeekData(
        "index",
        "Rest In Christ",
        "lessonIndex",
        "Living in a 24-7 Society",
        "cover",
        weekDays()
    )

    private fun weekDay(
        index: String = "index",
        title: String = "The Roots of Restlessness",
        date: String = "Sat, July 10"
    ): WeekDay = WeekDay(index, title, date)

    private fun weekDays() = listOf(
        weekDay(),
        weekDay(
            title = "Jesus Brings Division",
            date = "Sun, July 11"
        ),
        weekDay(
            title = "Selfishness",
            date = "Mon, July 12"
        ),
        weekDay(
            title = "Ambition",
            date = "Tue, July 13"
        ),
        weekDay(
            title = "Hypocrisy",
            date = "Wed, July 14"
        ),
        weekDay(
            title = "Uprooting Restlessness",
            date = "Thu, July 15"
        ),
        weekDay(
            title = "Further Thought",
            date = "Fri, July 16"
        )
    )
}
