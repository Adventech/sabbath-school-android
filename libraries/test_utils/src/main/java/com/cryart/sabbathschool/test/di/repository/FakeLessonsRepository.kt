package com.cryart.sabbathschool.test.di.repository

import app.ss.lessons.data.repository.lessons.LessonsRepository
import app.ss.models.SSDay
import app.ss.models.SSLessonInfo
import app.ss.models.SSRead
import com.cryart.sabbathschool.core.response.Resource
import com.cryart.sabbathschool.test.di.mock.MockDataFactory
import javax.inject.Inject

class FakeLessonsRepository @Inject constructor() : LessonsRepository {
    override suspend fun getLessonInfo(lessonIndex: String, cached: Boolean): Resource<SSLessonInfo> {
        return Resource.success(MockDataFactory.lessonInfo())
    }

    override suspend fun getDayRead(dayIndex: String): Resource<SSRead> {
        return Resource.success(MockDataFactory.ssRead(dayIndex))
    }

    override suspend fun getDayRead(day: SSDay): Resource<SSRead> {
        return Resource.success(MockDataFactory.ssRead(day.index))
    }

    override fun checkReaderArtifact() {}

    override suspend fun getPreferredBibleVersion(): String = MockDataFactory.versions.first()

    override suspend fun savePreferredBibleVersion(version: String) {}
}
