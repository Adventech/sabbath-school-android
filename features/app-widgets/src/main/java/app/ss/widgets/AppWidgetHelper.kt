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

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import app.ss.widgets.glance.today.TodayAppWidgetReceiver
import app.ss.widgets.glance.today.TodayImageAppWidgetReceiver
import app.ss.widgets.glance.week.LessonInfoWidgetReceiver
import app.ss.widgets.today.TodayAppWidget
import app.ss.widgets.today.TodayImgAppWidget
import app.ss.widgets.week.WeekLessonWidget
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface AppWidgetHelper {
    fun refreshAll()
}

internal class AppWidgetHelperImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : AppWidgetHelper {

    private val widgets = listOf(
        TodayAppWidget::class.java,
        TodayImgAppWidget::class.java,
        WeekLessonWidget::class.java,

        TodayAppWidgetReceiver::class.java,
        TodayImageAppWidgetReceiver::class.java,
        LessonInfoWidgetReceiver::class.java
    )

    override fun refreshAll() {
        with(context.applicationContext) {
            widgets.forEach { clazz ->
                sendBroadcast(
                    Intent(BaseWidgetProvider.REFRESH_ACTION)
                        .setComponent(ComponentName(context, clazz))
                )
            }
        }
    }
}
