package com.cryart.sabbathschool.test.di.repository

import app.ss.lessons.data.repository.lessons.LessonsRepository
import app.ss.models.PdfAnnotations
import app.ss.models.SSDay
import app.ss.models.SSLessonInfo
import app.ss.models.SSRead
import app.ss.models.SSReadComments
import app.ss.models.SSReadHighlights
import app.ss.models.TodayData
import app.ss.models.WeekData
import com.cryart.sabbathschool.core.response.Resource
import com.cryart.sabbathschool.test.di.mock.MockDataFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject

class FakeLessonsRepository @Inject constructor() : LessonsRepository {
    override suspend fun getLessonInfo(lessonIndex: String, cached: Boolean): Resource<SSLessonInfo> {
        return Resource.success(MockDataFactory.lessonInfo())
    }

    override suspend fun getTodayRead(cached: Boolean): Resource<TodayData> {
        return Resource.success(MockDataFactory.todayModel())
    }

    override suspend fun getDayRead(dayIndex: String): Resource<SSRead> {
        return Resource.success(MockDataFactory.ssRead(dayIndex))
    }

    override suspend fun getDayRead(day: SSDay): Resource<SSRead> {
        return Resource.success(MockDataFactory.ssRead(day.index))
    }

    override suspend fun getWeekData(cached: Boolean): Resource<WeekData> {
        return Resource.success(MockDataFactory.weekData())
    }

    override suspend fun saveAnnotations(lessonIndex: String, pdfId: String, annotations: List<PdfAnnotations>) {
    }

    override fun getAnnotations(lessonIndex: String, pdfId: String): Flow<Resource<List<PdfAnnotations>>> {
        return emptyFlow()
    }

    override suspend fun getComments(readIndex: String): Resource<SSReadComments> {
        return Resource.success(SSReadComments(readIndex, emptyList()))
    }

    override suspend fun saveComments(comments: SSReadComments) {}

    override suspend fun getReadHighlights(readIndex: String): Resource<SSReadHighlights> {
        return Resource.success(SSReadHighlights(readIndex))
    }

    override suspend fun saveHighlights(highlights: SSReadHighlights) {}

    override fun checkReaderArtifact() {}

    override suspend fun getPreferredBibleVersion(): String = MockDataFactory.versions.first()

    override suspend fun savePreferredBibleVersion(version: String) {}
}
