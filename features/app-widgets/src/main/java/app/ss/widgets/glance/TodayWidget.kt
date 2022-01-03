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

package app.ss.widgets.glance

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.Button
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.background
import androidx.glance.appwidget.cornerRadius
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import app.ss.widgets.R
import app.ss.widgets.WidgetDataProvider
import app.ss.widgets.glance.theme.TodayTitle
import app.ss.widgets.model.TodayWidgetModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

internal class TodayWidget(
    private val model: TodayWidgetModel,
) : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(
        setOf(DpSize(180.dp, 110.dp), DpSize(300.dp, 110.dp))
    )

    @Composable
    override fun Content() {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(day = Color.White, night = Color.Black)
                .appWidgetBackground()
                .cornerRadius(20.dp)
                .padding(8.dp)
        ) {
            Today(model = model)
        }
    }
}

private const val pkg = "com.cryart.sabbathschool"
private val cmp = ComponentName(pkg, "$pkg.ui.splash.SplashActivity")

@Composable
private fun Today(
    model: TodayWidgetModel,
    context: Context = LocalContext.current
) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.Bottom,
    ) {
        Spacer(
            modifier = GlanceModifier.defaultWeight()
        )
        Text(
            text = model.date,
            style = TodayTitle
        )
        Text(
            text = model.title,
            style = TodayTitle
        )
        Button(
            text = context.getString(R.string.ss_lessons_read),
            onClick = actionStartActivity(cmp)
        )
    }
}

@AndroidEntryPoint
internal class TodayWidgetProvider : GlanceAppWidgetReceiver() {

    @Inject
    lateinit var dataProvider: WidgetDataProvider

    private val model = TodayWidgetModel(
        "Further Thought",
        "Friday, December 31",
        "",
        Uri.EMPTY
    )

    override val glanceAppWidget: GlanceAppWidget get() = TodayWidget(model)
}
