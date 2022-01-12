/*
 * Copyright (c) 2022. Adventech <info@adventech.io>
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

package app.ss.widgets.glance.today

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.Text
import androidx.glance.unit.ColorProvider
import app.ss.widgets.R
import app.ss.widgets.WidgetDataProvider
import app.ss.widgets.glance.BaseGlanceAppWidget
import app.ss.widgets.glance.theme.AppWidgetCornerRadius
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
import com.cryart.sabbathschool.core.misc.DateHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

private typealias Data = TodayWidgetModel

internal class TodayAppWidget @AssistedInject constructor(
    private val dataProvider: WidgetDataProvider,
    @Assisted context: Context,
) : BaseGlanceAppWidget<Data?>(context = context) {

    override val sizeMode = SizeMode.Responsive(
        setOf(DpSize(180.dp, 110.dp), DpSize(300.dp, 110.dp))
    )

    override suspend fun loadData(): Data? = dataProvider.getTodayModel()

    @Composable
    override fun Content(data: Data?) {
        SsAppWidgetTheme {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .appWidgetBackground()
                    .cornerRadius(AppWidgetCornerRadius)
            ) {
                WidgetAppLogo()

                TodayInfo(model = data ?: errorModel())
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(context: Context): TodayAppWidget
    }
}

private const val pkg = "com.cryart.sabbathschool"
private val cmp = ComponentName(pkg, "$pkg.ui.splash.SplashActivity")

@Composable
internal fun errorModel(
    context: Context = LocalContext.current,
): TodayWidgetModel = TodayWidgetModel(
    context.getString(R.string.ss_widget_error_label),
    DateHelper.today(),
    "",
    Uri.EMPTY
)

@Composable
private fun WidgetAppLogo(
    modifier: GlanceModifier = GlanceModifier,
) {
    Row(modifier = modifier.fillMaxWidth()) {
        Spacer(modifier = GlanceModifier.defaultWeight())

        Image(
            provider = ImageProvider(R.drawable.ic_widget_logo),
            contentDescription = "",
            modifier = GlanceModifier
                .size(70.dp)
                .padding(
                    top = (-40).dp,
                    end = (-35).dp
                )
        )
    }
}

@Composable
internal fun TodayInfo(
    model: TodayWidgetModel,
    context: Context = LocalContext.current,
    modifier: GlanceModifier = GlanceModifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                start = Spacing12,
                end = Spacing12,
                bottom = Spacing12
            ),
        verticalAlignment = Alignment.Bottom,
    ) {
        Spacer(modifier = GlanceModifier.defaultWeight())

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
                .height(32.dp)
        )
    }
}
