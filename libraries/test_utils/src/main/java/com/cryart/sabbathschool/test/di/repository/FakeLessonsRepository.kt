package com.cryart.sabbathschool.test.di.repository

import app.ss.lessons.data.model.SSDay
import app.ss.lessons.data.model.SSLesson
import app.ss.lessons.data.model.SSLessonInfo
import app.ss.lessons.data.model.SSRead
import app.ss.lessons.data.repository.lessons.LessonsRepository
import com.cryart.sabbathschool.core.response.Resource
import javax.inject.Inject

class FakeLessonsRepository @Inject constructor() : LessonsRepository {
    override suspend fun getLessonInfo(lessonIndex: String): Resource<SSLessonInfo> {
        val lessonInfo = SSLessonInfo(
            SSLesson("Lesson", cover = "cover_url"),
            listOf(SSDay(title = "Day Title", date = "03/04/2021"))
        )
        return Resource.success(lessonInfo)
    }

    override suspend fun getDayRead(dayIndex: String): Resource<SSRead> {
        return Resource.success(SSRead("123"))
    }
}
