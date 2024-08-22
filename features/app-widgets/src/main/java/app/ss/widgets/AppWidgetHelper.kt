/*
 * Copyright (c) 2021. Adventech <info@adventech.io>
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
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package app.ss.widgets

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import app.ss.models.AppWidgetDay
import app.ss.widgets.glance.today.TodayAppWidget
import app.ss.widgets.glance.today.TodayImageAppWidget
import app.ss.widgets.glance.week.LessonInfoWidget
import app.ss.widgets.today.TodayAppWidgetProvider
import app.ss.widgets.today.TodayImgAppWidgetProvider
import app.ss.widgets.week.WeekLessonWidget
import app.ss.widgets.work.WidgetUpdateWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ss.foundation.coroutines.DispatcherProvider
import ss.foundation.coroutines.Scopable
import ss.foundation.coroutines.defaultScopable
import ss.libraries.storage.api.dao.AppWidgetDao
import ss.libraries.storage.api.dao.LessonsDao
import ss.libraries.storage.api.dao.QuarterliesDao
import ss.libraries.storage.api.entity.AppWidgetEntity
import ss.libraries.storage.api.entity.LessonEntity
import ss.misc.DateHelper.isNowInRange
import timber.log.Timber
import javax.inject.Inject

interface AppWidgetHelper {
    fun refreshAll()
    suspend fun isAdded(): Boolean
    fun syncQuarterly(index: String)
}

internal class AppWidgetHelperImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appWidgetDao: AppWidgetDao,
    private val lessonsDao: LessonsDao,
    private val quarterliesDao: QuarterliesDao,
    private val dispatcherProvider: DispatcherProvider,
) : AppWidgetHelper, Scopable by defaultScopable(dispatcherProvider) {

    private val exceptionLogger = CoroutineExceptionHandler { _, exception -> Timber.e(exception) }

    private val widgets = listOf(
        TodayAppWidgetProvider::class.java,
        TodayImgAppWidgetProvider::class.java,
        WeekLessonWidget::class.java,
    )

    override fun refreshAll() {
        scope.launch(exceptionLogger) {
            if (isAdded()) {
                with(context) {
                    widgets.forEach { clazz ->
                        sendBroadcast(
                            Intent(BaseWidgetProvider.REFRESH_ACTION)
                                .setComponent(ComponentName(context, clazz))
                        )
                    }
                }

                LessonInfoWidget().updateAll(context)
                TodayAppWidget().updateAll(context)
                TodayImageAppWidget().updateAll(context)

                WidgetUpdateWorker.enqueue(context)
            } else {
                WidgetUpdateWorker.cancel(context)
            }
        }
    }

    override suspend fun isAdded(): Boolean {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val glanceAppWidgetManager = GlanceAppWidgetManager(context)

        val glanceWidgetIds = withContext(dispatcherProvider.default) {
            glanceAppWidgetManager.getGlanceIds(LessonInfoWidget().javaClass) +
                glanceAppWidgetManager.getGlanceIds(TodayAppWidget().javaClass) +
                glanceAppWidgetManager.getGlanceIds(TodayImageAppWidget().javaClass)

        }
        val legacyWidgetIds = widgets.map { appWidgetManager.getAppWidgetIds(ComponentName(context, it)) }

        return (glanceWidgetIds + legacyWidgetIds).isNotEmpty()
    }

    override fun syncQuarterly(index: String) {
        refreshAll()

        scope.launch(exceptionLogger) {
            val quarterlyInfo = quarterliesDao.getInfo(index) ?: return@launch

            // Only sync the latest quarterly
            if (isNowInRange(quarterlyInfo.quarterly.start_date, quarterlyInfo.quarterly.end_date)) {
                val lessonIndex = quarterlyInfo.lessons.firstOrNull { it.isCurrent() }?.index ?: return@launch
                val lesson = lessonsDao.get(lessonIndex) ?: return@launch

                val entity = AppWidgetEntity(
                    quarterlyIndex = index,
                    cover = quarterlyInfo.quarterly.cover,
                    title = quarterlyInfo.quarterly.title,
                    description = lesson.title,
                    days = lesson.days.mapIndexed { index, day ->
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
    }

    private fun LessonEntity.isCurrent(): Boolean {
        return isNowInRange(start_date, end_date)
    }
}
