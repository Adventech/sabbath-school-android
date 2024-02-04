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
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.withContext
import ss.foundation.coroutines.DispatcherProvider
import timber.log.Timber

internal interface WidgetDataProvider {

    suspend fun getTodayModel(): TodayWidgetModel?

    suspend fun getWeekLessonModel(): WeekLessonWidgetModel?

    suspend fun sync()
}

@Singleton
internal class WidgetDataProviderImpl @Inject constructor(
    private val widgetAction: AppWidgetAction,
    private val repository: LessonsRepository,
    private val dispatcherProvider: DispatcherProvider,
) : WidgetDataProvider {

    override suspend fun getTodayModel(): TodayWidgetModel? = fetchTodayModel(cached = true)

    private suspend fun fetchTodayModel(cached: Boolean) = try {
        withContext(dispatcherProvider.io) {
            val response = repository.getTodayRead(cached)
            if (response.isSuccessFul) {
                response.data?.let { data ->
                    TodayWidgetModel(
                        title = data.title,
                        date = data.date,
                        cover = data.cover,
                        intent = when {
                            data.quarterlyIndex.isNullOrEmpty() ->
                                widgetAction.launchRead(data.lessonIndex)
                            else -> widgetAction.launchLesson(data.quarterlyIndex!!)
                        },
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
                val lessonsIntent = widgetAction.launchLesson(data.quarterlyIndex)

                val days = data.days.mapIndexed { index, day ->
                    WeekDayWidgetModel(
                        title = day.title,
                        date = day.date,
                        intent = widgetAction.launchRead(data.lessonIndex, index.toString())
                            .takeUnless { day.index == data.quarterlyIndex } ?: lessonsIntent,
                        today = day.today
                    )
                }

                WeekLessonWidgetModel(
                    quarterlyTitle = data.quarterlyTitle,
                    lessonTitle = data.lessonTitle,
                    cover = data.cover,
                    days = days,
                    intent = lessonsIntent,
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
