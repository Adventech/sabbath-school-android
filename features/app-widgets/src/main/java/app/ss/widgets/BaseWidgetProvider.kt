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

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import app.ss.widgets.data.AppWidgetRepository
import app.ss.widgets.model.WidgetType
import app.ss.widgets.today.TodayAppWidgetProvider
import app.ss.widgets.today.TodayImgAppWidgetProvider
import app.ss.widgets.week.WeekLessonWidget
import com.cryart.sabbathschool.core.extensions.sdk.isAtLeastApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

abstract class BaseWidgetProvider<M> : AppWidgetProvider() {

    internal abstract val type: WidgetType

    abstract fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        state: M?
    )

    @Inject
    lateinit var repository: AppWidgetRepository

    private val job = SupervisorJob()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + job)

    override fun onReceive(context: Context, widgetIntent: Intent) {
        val action = widgetIntent.action
        if (REFRESH_ACTION == action) {
            Timber.d("received REFRESH_ACTION from widget")

            // Instruct the widget manager to update the widget
            val mgr = AppWidgetManager.getInstance(context)
            val clazz = when (type) {
                WidgetType.TODAY -> TodayAppWidgetProvider::class.java
                WidgetType.TODAY_IMG -> TodayImgAppWidgetProvider::class.java
                WidgetType.WEEK_LESSON -> WeekLessonWidget::class.java
            }
            val cn = ComponentName(context, clazz)
            val ids = mgr.getAppWidgetIds(cn)
            onUpdate(context, mgr, ids)
        } else {
            super.onReceive(context, widgetIntent)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        coroutineScope.launch {
            val stateFlow: Flow<M?> = when (type) {
                WidgetType.TODAY,
                WidgetType.TODAY_IMG -> repository.todayState() as? Flow<M?>
                WidgetType.WEEK_LESSON -> repository.weekState() as? Flow<M?>
            } ?: return@launch

            stateFlow.collect { state ->
                withContext(Dispatchers.Main) {
                    for (appWidgetId in appWidgetIds) {
                        updateAppWidget(context, appWidgetManager, appWidgetId, state)
                    }
                }
            }
        }
    }

    companion object {
        internal const val REFRESH_ACTION = "app.ss.appwidget.action.REFRESH"
    }
}

internal fun Intent.clickIntent(context: Context): PendingIntent {
    val flag = if (isAtLeastApi(Build.VERSION_CODES.M)) PendingIntent.FLAG_IMMUTABLE else 0
    return PendingIntent.getActivity(context, 0, this, flag)
}
