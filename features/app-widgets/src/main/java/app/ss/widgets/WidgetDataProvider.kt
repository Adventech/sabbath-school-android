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

package app.ss.widgets

import app.ss.lessons.data.repository.lessons.LessonsRepository
import app.ss.widgets.model.TodayWidgetModel
import app.ss.widgets.model.WeekDayWidgetModel
import app.ss.widgets.model.WeekLessonWidgetModel
import com.cryart.sabbathschool.core.navigation.Destination
import com.cryart.sabbathschool.core.navigation.toUri
import kotlinx.coroutines.withContext
import ss.foundation.coroutines.DispatcherProvider
import ss.misc.SSConstants
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

internal interface WidgetDataProvider {

    suspend fun getTodayModel(): TodayWidgetModel?

    suspend fun getWeekLessonModel(): WeekLessonWidgetModel?

    suspend fun sync()
}

@Singleton
internal class WidgetDataProviderImpl @Inject constructor(
    private val repository: LessonsRepository,
    private val dispatcherProvider: DispatcherProvider,
) : WidgetDataProvider {

    override suspend fun getTodayModel(): TodayWidgetModel? = fetchTodayModel(cached = true)

    private suspend fun fetchTodayModel(cached: Boolean) = try {
        withContext(dispatcherProvider.io) {
            val response = repository.getTodayRead(cached)
            if (response.isSuccessFul) {
                response.data?.let { data ->
                    val uri = when {
                        data.quarterlyIndex.isNullOrEmpty() ->
                            Destination.READ.toUri(SSConstants.SS_LESSON_INDEX_EXTRA to data.lessonIndex)

                        else -> Destination.LESSONS.toUri(
                            SSConstants.SS_QUARTERLY_INDEX_EXTRA to data.quarterlyIndex!!
                        )
                    }
                    TodayWidgetModel(
                        data.title,
                        data.date,
                        data.cover,
                        uri,
                    )
                }
            } else {
                Timber.e(response.error)
                null
            }
        }
    } catch (ex: Exception) {
        Timber.e(ex)
        null
    }

    override suspend fun getWeekLessonModel(): WeekLessonWidgetModel? = fetchWeekLessonModel(cached = true)

    private suspend fun fetchWeekLessonModel(cached: Boolean) = try {
        withContext(dispatcherProvider.io) {
            repository.getWeekData(cached = cached).data?.let { data ->
                val lessonsUri = Destination.LESSONS.toUri(
                    SSConstants.SS_QUARTERLY_INDEX_EXTRA to data.quarterlyIndex
                )
                val days = data.days.mapIndexed { index, day ->
                    WeekDayWidgetModel(
                        day.title,
                        day.date,
                        Destination.READ.toUri(
                            SSConstants.SS_LESSON_INDEX_EXTRA to data.lessonIndex,
                            SSConstants.SS_READ_POSITION_EXTRA to index.toString()
                        ).takeUnless { day.index == data.quarterlyIndex } ?: lessonsUri,
                        day.today
                    )
                }

                WeekLessonWidgetModel(
                    data.quarterlyTitle,
                    data.lessonTitle,
                    data.cover,
                    days,
                    lessonsUri
                )
            }
        }
    } catch (ex: Exception) {
        Timber.e(ex)
        null
    }

    override suspend fun sync() {
        fetchTodayModel(cached = false)
        fetchWeekLessonModel(cached = false)
    }
}
