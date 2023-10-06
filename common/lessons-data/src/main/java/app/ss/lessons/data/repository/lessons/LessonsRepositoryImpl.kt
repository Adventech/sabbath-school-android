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

package app.ss.lessons.data.repository.lessons

import app.ss.lessons.data.model.QuarterlyLessonInfo
import app.ss.lessons.data.repository.quarterly.QuarterliesDataSource
import app.ss.lessons.data.repository.quarterly.QuarterlyInfoDataSource
import app.ss.models.SSDay
import app.ss.models.SSLessonInfo
import app.ss.models.SSQuarterlyInfo
import app.ss.models.SSRead
import app.ss.models.TodayData
import app.ss.models.WeekData
import app.ss.models.WeekDay
import app.ss.storage.db.dao.BibleVersionDao
import app.ss.storage.db.entity.BibleVersionEntity
import com.cryart.sabbathschool.core.response.Resource
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import ss.foundation.coroutines.DispatcherProvider
import ss.misc.DateHelper.formatDate
import ss.misc.DateHelper.parseDate
import ss.misc.SSConstants
import ss.prefs.api.SSPrefs
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class LessonsRepositoryImpl @Inject constructor(
    private val ssPrefs: SSPrefs,
    private val quarterliesDataSource: QuarterliesDataSource,
    private val quarterlyInfoDataSource: QuarterlyInfoDataSource,
    private val lessonInfoDataSource: LessonInfoDataSource,
    private val readsDataSource: ReadsDataSource,
    private val bibleVersionDao: BibleVersionDao,
    private val dispatcherProvider: DispatcherProvider,
    private val readerArtifactHelper: ReaderArtifactHelper
) : LessonsRepository {

    private val today = DateTime.now().withTimeAtStartOfDay()

    override suspend fun getLessonInfo(lessonIndex: String, cached: Boolean): Resource<SSLessonInfo> {
        return if (cached) {
            withContext(dispatcherProvider.io) {
                lessonInfoDataSource.cache.getItem(LessonInfoDataSource.Request(lessonIndex))
            }
        } else {
            lessonInfoDataSource.getItem(LessonInfoDataSource.Request(lessonIndex))
        }
    }

    override suspend fun getTodayRead(cached: Boolean): Resource<TodayData> {
        val dataResponse = getQuarterlyAndLessonInfo(cached)
        val data = dataResponse.data ?: return Resource.error(dataResponse.error ?: Throwable("Invalid QuarterlyInfo"))
        val lessonInfo = data.lessonInfo
        val quarterlyInfo = data.quarterlyInfo

        if (lessonInfo.days.isEmpty()) {
            return Resource.success(
                TodayData(
                    lessonInfo.lesson.index,
                    lessonInfo.lesson.index,
                    lessonInfo.lesson.title,
                    formatDate(lessonInfo.lesson.start_date),
                    quarterlyInfo.quarterly.cover,
                    lessonInfo.lesson.cover,
                    quarterlyIndex = quarterlyInfo.quarterly.index
                )
            )
        }

        val todayModel = lessonInfo.days.find { day ->
            today.isEqual(parseDate(day.date))
        }?.let { day ->
            TodayData(
                day.index,
                lessonInfo.lesson.index,
                day.title,
                formatDate(day.date),
                quarterlyInfo.quarterly.cover,
                lessonInfo.lesson.cover,
                null
            )
        } ?: return Resource.error(Throwable("Error Finding Today Read"))

        return Resource.success(todayModel)
    }

    private suspend fun getQuarterlyAndLessonInfo(cached: Boolean): Resource<QuarterlyLessonInfo> {
        val quarterlyResponse = getQuarterlyInfo()
        val quarterlyInfo = quarterlyResponse.data ?: return Resource.error(quarterlyResponse.error ?: Throwable("Invalid QuarterlyInfo"))
        val lessonInfo = getWeekLessonInfo(quarterlyInfo, cached) ?: return Resource.error(Throwable("Invalid LessonInfo"))

        return Resource.success(QuarterlyLessonInfo(quarterlyInfo, lessonInfo))
    }

    private suspend fun getQuarterlyInfo(): Resource<SSQuarterlyInfo> {
        val index = getLastQuarterlyInfoIfCurrent()?.let {
            return Resource.success(it)
        } ?: getDefaultQuarterlyIndex() ?: return Resource.error(Throwable("Invalid Quarterly Index"))

        return withContext(dispatcherProvider.io) {
            quarterlyInfoDataSource.cache.getItem(QuarterlyInfoDataSource.Request(index))
        }
    }

    private suspend fun getLastQuarterlyInfoIfCurrent(): SSQuarterlyInfo? {
        val index = ssPrefs.getLastQuarterlyIndex() ?: return null
        val info = withContext(dispatcherProvider.io) {
            quarterlyInfoDataSource.cache.getItem(QuarterlyInfoDataSource.Request(index)).data
        } ?: return null

        return if (today.isBefore(parseDate(info.quarterly.end_date))) {
            info
        } else {
            null
        }
    }

    private suspend fun getDefaultQuarterlyIndex(): String? {
        val resource = withContext(dispatcherProvider.io) {
            quarterliesDataSource.cache.get(QuarterliesDataSource.Request(ssPrefs.getLanguageCode()))
        }
        val quarterly = resource.data?.firstOrNull()
        return quarterly?.index
    }

    private suspend fun getWeekLessonInfo(quarterlyInfo: SSQuarterlyInfo, cached: Boolean): SSLessonInfo? {
        val lesson = quarterlyInfo.lessons.find { lesson ->
            val startDate = parseDate(lesson.start_date)
            val endDate = parseDate(lesson.end_date)

            startDate?.isBeforeNow == true && (endDate?.isAfterNow == true || today.isEqual(endDate))
        }

        return lesson?.let { getLessonInfo(it.index, cached).data }
    }

    override suspend fun getDayRead(dayIndex: String): Resource<SSRead> = withContext(dispatcherProvider.io) {
        readsDataSource.cache.getItem(
            ReadsDataSource.Request(dayIndex = dayIndex, fullPath = "")
        )
    }

    override suspend fun getDayRead(day: SSDay): Resource<SSRead> =
        readsDataSource.getItem(
            ReadsDataSource.Request(
                dayIndex = day.index,
                fullPath = day.full_read_path
            )
        )

    override suspend fun getWeekData(cached: Boolean): Resource<WeekData> {
        val dataResponse = getQuarterlyAndLessonInfo(cached)
        val (quarterlyInfo, lessonInfo) = dataResponse.data ?: return Resource.error(dataResponse.error ?: Throwable("Invalid QuarterlyInfo"))

        val days = if (lessonInfo.days.isEmpty()) {
            listOf(
                WeekDay(
                    index = quarterlyInfo.quarterly.index,
                    title = lessonInfo.lesson.title,
                    date = formatDate(lessonInfo.lesson.start_date, SSConstants.SS_DATE_FORMAT_OUTPUT_DAY_SHORT),
                    today = false
                )
            )
        } else {
            lessonInfo.days.map { ssDay ->
                WeekDay(
                    ssDay.index,
                    ssDay.title,
                    formatDate(ssDay.date, SSConstants.SS_DATE_FORMAT_OUTPUT_DAY_SHORT),
                    today.isEqual(parseDate(ssDay.date))
                )
            }.take(7)
        }

        return Resource.success(
            WeekData(
                quarterlyInfo.quarterly.index,
                quarterlyInfo.quarterly.title,
                lessonInfo.lesson.index,
                lessonInfo.lesson.title,
                quarterlyInfo.quarterly.cover,
                days
            )
        )
    }

    override fun checkReaderArtifact() {
        readerArtifactHelper.sync()
    }

    override suspend fun getPreferredBibleVersion(): String? = withContext(dispatcherProvider.io) {
        bibleVersionDao.get(ssPrefs.getLanguageCode())?.version
    }

    override suspend fun savePreferredBibleVersion(version: String) = withContext(dispatcherProvider.io) {
        bibleVersionDao.insertItem(BibleVersionEntity(ssPrefs.getLanguageCode(), version))
    }
}
