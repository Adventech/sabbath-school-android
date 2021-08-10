package com.cryart.sabbathschool.test.di.repository

import app.ss.lessons.data.model.SSBibleVerses
import app.ss.lessons.data.model.SSDay
import app.ss.lessons.data.model.SSLesson
import app.ss.lessons.data.model.SSLessonInfo
import app.ss.lessons.data.model.SSRead
import app.ss.lessons.data.model.TodayData
import app.ss.lessons.data.model.WeekData
import app.ss.lessons.data.model.WeekDay

/**
 * Mock data for UI tests
 */
object MockDataFactory {

    const val QUARTERLY_INDEX = ""

    val versions = listOf(
        "NASB", "NKJV", "KJV"
    )
    val verses = mapOf(
        "1John14" to "<h2>1 John 1:4</h2><sup>4</sup> These things we write, so that our joy may be made complete."
    )

    private val bibleVerses: List<SSBibleVerses> = versions.map { version ->
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
        "cover"
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
