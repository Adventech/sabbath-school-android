package com.cryart.sabbathschool.test.di.repository

import app.ss.lessons.data.model.SSBibleVerses
import app.ss.lessons.data.model.SSDay
import app.ss.lessons.data.model.SSLesson
import app.ss.lessons.data.model.SSLessonInfo
import app.ss.lessons.data.model.SSRead

/**
 * Mock data for UI tests
 */
object MockDataFactory {

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
}
