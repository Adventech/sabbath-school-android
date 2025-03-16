/*
 * Copyright (c) 2025. Adventech <info@adventech.io>
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

package ss.lessons.impl.helper

import app.ss.models.AppWidgetDay
import app.ss.models.OfflineState
import app.ss.models.SSLesson
import app.ss.models.SSQuarterlyInfo
import app.ss.network.NetworkResource
import app.ss.network.safeApiCall
import kotlinx.coroutines.withContext
import ss.foundation.android.connectivity.ConnectivityHelper
import ss.foundation.coroutines.DispatcherProvider
import ss.lessons.api.SSQuarterliesApi
import ss.lessons.api.helper.SyncHelper
import ss.lessons.api.repository.LessonsRepository
import ss.lessons.impl.ext.toEntity
import ss.lessons.impl.ext.toModel
import ss.libraries.storage.api.dao.AppWidgetDao
import ss.libraries.storage.api.dao.LessonsDao
import ss.libraries.storage.api.dao.QuarterliesDao
import ss.libraries.storage.api.entity.AppWidgetEntity
import ss.misc.DateHelper.isNowInRange
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class SyncHelperImpl @Inject constructor(
    private val quarterliesApi: SSQuarterliesApi,
    private val quarterliesDao: QuarterliesDao,
    private val lessonsDao: LessonsDao,
    private val appWidgetDao: AppWidgetDao,
    private val lessonsRepository: LessonsRepository,
    private val connectivityHelper: ConnectivityHelper,
    private val dispatcherProvider: DispatcherProvider
) : SyncHelper {

    override suspend fun syncAppWidgetInfo(quarterlyIndex: String, skipCache: Boolean) {
        val cached = withContext(dispatcherProvider.io) {
            quarterliesDao.getInfo(quarterlyIndex)?.takeUnless { it.lessons.isEmpty() }
        }
        val quarterlyInfo = (if (cached == null || skipCache) {
            quarterlyInfoFromNetwork(quarterlyIndex)
        } else {
            cached.toModel()
        }) ?: return

        val lesson = quarterlyInfo.lessons.firstOrNull { it.isCurrent() } ?: return
        val lessonIndex = lesson.index
        val lessonPath = lesson.path
        lessonsRepository.getLessonInfoResult(lessonIndex, lessonPath, skipCache).onSuccess { info ->
            val (lesson, days, _) = info

            val entity = AppWidgetEntity(
                quarterlyIndex = quarterlyIndex,
                cover = quarterlyInfo.quarterly.cover,
                title = quarterlyInfo.quarterly.title,
                description = lesson.title,
                days = days.mapIndexed { index, day ->
                    AppWidgetDay(
                        lessonIndex = lesson.index,
                        dayIndex = index,
                        title = day.title,
                        date = day.date,
                        image = lesson.cover,
                    )
                }
            )

            appWidgetDao.insertItem(entity)
        }
    }

    private suspend fun quarterlyInfoFromNetwork(index: String): SSQuarterlyInfo? {
        val language = index.substringBefore('-')
        val id = index.substringAfter('-')

        return when (val response = safeApiCall(connectivityHelper) { quarterliesApi.getQuarterlyInfo(language, id) }) {
            is NetworkResource.Failure -> {
                Timber.e("Failed to fetch Quarterly: isNetwork=${response.isNetworkError}, ${response.errorBody}")
                null
            }

            is NetworkResource.Success -> response.value.body()?.let { quarterlyInfo ->
                saveQuarterlyInfo(quarterlyInfo)
                quarterlyInfo
            }
        }
    }

    private suspend fun saveQuarterlyInfo(info: SSQuarterlyInfo) {
        val quarterIndex = info.quarterly.index
        info.lessons.forEachIndexed { index, lesson ->
            lessonsDao.get(lesson.index)?.let { entity ->
                lessonsDao.update(
                    lesson.toEntity(
                        quarter = quarterIndex,
                        order = index,
                        days = entity.days,
                        pdfs = entity.pdfs,
                    )
                )
            } ?: run { lessonsDao.insertItem(lesson.toEntity(quarterIndex, order = index)) }
        }
        val state = quarterliesDao.getOfflineState(info.quarterly.index) ?: OfflineState.NONE
        quarterliesDao.insertItem(info.quarterly.toEntity(state))
    }

    private fun SSLesson.isCurrent(): Boolean {
        return isNowInRange(start_date, end_date)
    }
}
