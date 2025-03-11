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

package app.ss.widgets.glance.week

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.BitmapImageProvider
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.Text
import app.ss.widgets.glance.BaseGlanceAppWidget
import app.ss.widgets.glance.extensions.clickable
import app.ss.widgets.glance.extensions.stringResource
import app.ss.widgets.glance.layout.RoundedScrollingLazyColumn
import app.ss.widgets.glance.theme.SsGlanceTheme
import app.ss.widgets.glance.theme.todayBody
import app.ss.widgets.glance.theme.todayTitle
import app.ss.widgets.model.WeekDayModel
import app.ss.widgets.model.WeekWidgetState
import app.ss.translations.R as L10nR
import app.ss.widgets.R as WidgetsR

internal class LessonInfoWidget : BaseGlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Responsive(setOf(smallMode, mediumMode, largeMode))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = repository(context)

        provideContent {
            val state by repository.weekState(context).collectAsState(initial = WeekWidgetState.Loading)
            SsGlanceTheme { LessonInfoWidgetContent(state) }
        }
    }

    companion object {
        private val smallMode = DpSize(300.dp, 220.dp)
        private val mediumMode = DpSize(300.dp, 300.dp)
        private val largeMode = DpSize(300.dp, 400.dp)
    }
}

@Composable
private fun LessonInfoWidgetContent(state: WeekWidgetState, modifier: GlanceModifier = GlanceModifier) {
    Scaffold(modifier = modifier, horizontalPadding = 0.dp) {
        when (state) {
            WeekWidgetState.Error -> Unit
            WeekWidgetState.Loading -> LoadingContent()
            is WeekWidgetState.Success -> SuccessContent(state)
        }
    }
}

@Composable
private fun SuccessContent(state: WeekWidgetState.Success, modifier: GlanceModifier = GlanceModifier) {
    val model = state.model
    Column(modifier = modifier.fillMaxSize()) {

        LessonInfoRow(
            quarterlyTitle = model.title,
            lessonTitle = model.description,
            cover = model.image,
            modifier = GlanceModifier.clickable(intent = state.lessonIntent),
        )

        Box(
            modifier = GlanceModifier
                .defaultWeight()
                .padding(horizontal = 12.dp),
        ) {
            RoundedScrollingLazyColumn(
                items = model.days,
                itemContentProvider = { item ->
                    DayInfo(
                        model = item,
                        modifier = GlanceModifier.fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .background(if (item.today) GlanceTheme.colors.primary else GlanceTheme.colors.secondaryContainer)
                            .cornerRadius(8.dp)
                            .clickable(intent = item.intent)
                    )
                },
                modifier = GlanceModifier.fillMaxSize(),
                contentCornerRadius = 8.dp,
                verticalItemsSpacing = 8.dp,
            )
        }

        Spacer(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(8.dp)
        )
    }
}

@Composable
@SuppressLint("RestrictedApi")
private fun LessonInfoRow(
    quarterlyTitle: String,
    lessonTitle: String,
    cover: Bitmap?,
    modifier: GlanceModifier = GlanceModifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        cover?.let { bitmap ->
            Image(
                provider = BitmapImageProvider(bitmap),
                contentDescription = quarterlyTitle,
                modifier =
                    GlanceModifier.background(colorProvider = GlanceTheme.colors.surfaceVariant)
                        .size(width = CoverWidth, height = CoverHeight)
                        .cornerRadius(6.dp),
                contentScale = ContentScale.Crop,
            )
        }
            ?: run {
                Spacer(
                    modifier =
                        GlanceModifier.background(colorProvider = GlanceTheme.colors.surfaceVariant)
                            .size(width = CoverWidth, height = CoverHeight)
                            .cornerRadius(8.dp),
                )
            }

        Column(
            modifier = GlanceModifier.defaultWeight().padding(horizontal = 16.dp),
        ) {
            Text(
                text = quarterlyTitle,
                style = todayTitle(),
                maxLines = 2,
                modifier = GlanceModifier.fillMaxWidth(),
            )

            Spacer(modifier = GlanceModifier.height(4.dp).fillMaxWidth())

            Text(
                text = lessonTitle,
                style = todayBody().copy(fontSize = 12.sp),
                maxLines = 2,
                modifier = GlanceModifier.fillMaxWidth(),
            )
        }

        Column(modifier = GlanceModifier) {
            Image(
                provider = ImageProvider(WidgetsR.drawable.ic_widget_logo),
                contentDescription = stringResource(L10nR.string.ss_app_name),
                modifier = GlanceModifier.size(AppLogoSize),
            )
            Spacer(modifier = GlanceModifier.height(CoverHeight - AppLogoSize))
        }
    }
}

private val CoverWidth = 64.dp
private val CoverHeight = 100.dp
private val AppLogoSize = 48.dp

@Composable
private fun DayInfo(model: WeekDayModel, modifier: GlanceModifier = GlanceModifier) {
    val textStyle = todayBody(
        if (model.today) GlanceTheme.colors.onPrimary else GlanceTheme.colors.onSecondaryContainer
    )

    Row(
        modifier = modifier.padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = model.title,
            style = textStyle,
            maxLines = 2,
            modifier = GlanceModifier.defaultWeight(),
        )

        Spacer(modifier = GlanceModifier.width(8.dp))

        Text(
            text = model.date,
            style = textStyle.copy(fontSize = 13.sp),
            maxLines = 1,
            modifier = GlanceModifier,
        )
    }
}

@Composable
private fun LoadingContent(modifier: GlanceModifier = GlanceModifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            Text(
                text = "Loading...",
                style = todayBody(color = GlanceTheme.colors.onSecondaryContainer),
                modifier = GlanceModifier
                    .padding(vertical = 24.dp, horizontal = 32.dp)
                    .background(GlanceTheme.colors.secondaryContainer)
                    .cornerRadius(12.dp)
            )
        }
    }
}
