package com.cryart.sabbathschool.test.di.repository

import app.ss.models.PdfAnnotations
import app.ss.models.SSLessonInfo
import app.ss.models.SSRead
import app.ss.lessons.data.model.TodayData
import app.ss.lessons.data.model.WeekData
import app.ss.lessons.data.repository.lessons.LessonsRepository
import com.cryart.sabbathschool.core.response.Resource
import com.cryart.sabbathschool.test.di.mock.MockDataFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject

class FakeLessonsRepository @Inject constructor() : LessonsRepository {
    override suspend fun getLessonInfo(lessonIndex: String): Resource<SSLessonInfo> {
        return Resource.success(MockDataFactory.lessonInfo())
    }

    override suspend fun getTodayRead(): Resource<TodayData> {
        return Resource.success(MockDataFactory.todayModel())
    }

    override suspend fun getDayRead(dayIndex: String): Resource<SSRead> {
        return Resource.success(MockDataFactory.ssRead(dayIndex))
    }

    override suspend fun getWeekData(): Resource<WeekData> {
        return Resource.success(MockDataFactory.weekData())
    }

    override suspend fun saveAnnotations(lessonIndex: String, pdfId: String, annotations: List<PdfAnnotations>) {
    }

    override suspend fun getAnnotations(lessonIndex: String, pdfId: String): Flow<Resource<List<PdfAnnotations>>> {
        return emptyFlow()
    }
}
