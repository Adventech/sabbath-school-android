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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.unit.ColorProvider
import app.ss.widgets.R
import app.ss.widgets.glance.theme.SsAppWidgetTheme
import app.ss.widgets.glance.theme.copy
import app.ss.widgets.glance.theme.todayBody
import app.ss.widgets.glance.theme.todayTitle
import app.ss.widgets.model.TodayWidgetModel
import com.cryart.design.theme.Spacing12
import com.cryart.design.theme.Spacing20
import com.cryart.design.theme.Spacing32
import com.cryart.design.theme.Spacing4
import com.cryart.design.theme.Spacing6
import com.cryart.design.theme.Spacing8

internal class TodayWidget(
    private val model: TodayWidgetModel,
) : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(
        setOf(DpSize(180.dp, 110.dp), DpSize(300.dp, 110.dp))
    )

    @Composable
    override fun Content() {
        SsAppWidgetTheme {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .appWidgetBackground()
                    .cornerRadius(20.dp)
                    .padding(Spacing8)
            ) {
                Today(model = model)
            }
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
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(Spacing8),
        verticalAlignment = Alignment.Bottom,
    ) {
        Spacer(
            modifier = GlanceModifier.defaultWeight()
        )
        Text(
            text = model.date,
            style = todayBody(),
            maxLines = 2
        )

        Spacer(modifier = GlanceModifier.height(Spacing6))

        Text(
            text = model.title,
            style = todayTitle(),
            maxLines = 3
        )

        Spacer(modifier = GlanceModifier.height(Spacing12))

        Button(
            text = context.getString(R.string.ss_lessons_read).uppercase(),
            style = todayTitle().copy(
                color = ColorProvider(MaterialTheme.colorScheme.onPrimary),
                fontSize = 14.sp
            ),
            maxLines = 1,
            onClick = actionStartActivity(cmp),
            modifier = GlanceModifier
                .background(MaterialTheme.colorScheme.primary)
                .cornerRadius(Spacing20)
                .padding(horizontal = Spacing32, vertical = Spacing4)
        )
    }
}
