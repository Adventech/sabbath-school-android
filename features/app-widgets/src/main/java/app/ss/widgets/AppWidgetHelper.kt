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
import app.ss.widgets.glance.today.TodayImageAppWidget
import app.ss.widgets.glance.week.LessonInfoWidget
import app.ss.widgets.today.TodayAppWidget
import app.ss.widgets.today.TodayImgAppWidget
import app.ss.widgets.week.WeekLessonWidget
import app.ss.widgets.work.WidgetUpdateWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ss.foundation.coroutines.DispatcherProvider
import ss.foundation.coroutines.Scopable
import ss.foundation.coroutines.defaultScopable
import javax.inject.Inject

interface AppWidgetHelper {
    fun refreshAll()
    suspend fun isAdded(): Boolean
}

internal class AppWidgetHelperImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val lessonInfoWidget: LessonInfoWidget.Factory,
    private val todayWidget: app.ss.widgets.glance.today.TodayAppWidget.Factory,
    private val todayImageAppWidget: TodayImageAppWidget.Factory,
    private val dispatcherProvider: DispatcherProvider,
) : AppWidgetHelper, Scopable by defaultScopable(dispatcherProvider) {

    private val widgets = listOf(
        TodayAppWidget::class.java,
        TodayImgAppWidget::class.java,
        WeekLessonWidget::class.java,
    )

    override fun refreshAll() {
        scope.launch {
            if (isAdded()) {
                with(context) {
                    widgets.forEach { clazz ->
                        sendBroadcast(
                            Intent(BaseWidgetProvider.REFRESH_ACTION)
                                .setComponent(ComponentName(context, clazz))
                        )
                    }
                }

                lessonInfoWidget.create().updateAll(context)
                todayWidget.create().updateAll(context)
                todayImageAppWidget.create().updateAll(context)

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
            glanceAppWidgetManager.getGlanceIds(lessonInfoWidget.create().javaClass) +
                glanceAppWidgetManager.getGlanceIds(todayWidget.create().javaClass) +
                glanceAppWidgetManager.getGlanceIds(todayImageAppWidget.create().javaClass)

        }
        val legacyWidgetIds = widgets.map { appWidgetManager.getAppWidgetIds(ComponentName(context, it)) }

        return (glanceWidgetIds + legacyWidgetIds).isNotEmpty()
    }
}
