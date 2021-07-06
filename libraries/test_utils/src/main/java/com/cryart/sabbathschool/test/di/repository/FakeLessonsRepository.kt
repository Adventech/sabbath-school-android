package com.cryart.sabbathschool.test.di.repository

import app.ss.lessons.data.model.SSLessonInfo
import app.ss.lessons.data.model.SSRead
import app.ss.lessons.data.model.TodayModel
import app.ss.lessons.data.repository.lessons.LessonsRepository
import com.cryart.sabbathschool.core.response.Resource
import javax.inject.Inject

class FakeLessonsRepository @Inject constructor() : LessonsRepository {
    override suspend fun getLessonInfo(lessonIndex: String): Resource<SSLessonInfo> {
        return Resource.success(MockDataFactory.lessonInfo())
    }

    override suspend fun getTodayRead(): Resource<TodayModel?> {
        return Resource.success(MockDataFactory.todayModel())
    }

    override suspend fun getDayRead(dayIndex: String): Resource<SSRead> {
        return Resource.success(MockDataFactory.ssRead(dayIndex))
    }
}
