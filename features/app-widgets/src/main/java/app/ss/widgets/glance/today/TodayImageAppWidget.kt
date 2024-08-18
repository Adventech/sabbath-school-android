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

package app.ss.widgets.glance.today

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.BitmapImageProvider
import androidx.glance.ButtonDefaults
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Box
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.unit.ColorProvider
import app.ss.widgets.R
import app.ss.widgets.glance.BaseGlanceAppWidget
import app.ss.widgets.glance.extensions.clickable
import app.ss.widgets.glance.theme.SsGlanceTheme
import app.ss.widgets.model.TodayWidgetModel
import com.cryart.sabbathschool.core.extensions.context.fetchBitmap

internal class TodayImageAppWidget : BaseGlanceAppWidget() {

    data class Data(
        val model: TodayWidgetModel? = null,
        val modelCover: Bitmap? = null
    )

    override val sizeMode = SizeMode.Responsive(
        setOf(DpSize(180.dp, 55.dp), DpSize(180.dp, 110.dp), DpSize(300.dp, 110.dp))
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val model = dataProvider(context).getTodayModel()
        val bitmap = context.fetchBitmap(model?.cover)

        provideContent {
            SsGlanceTheme {
                Content(model = model, cover = bitmap)
            }
        }
    }

    @Composable
    @SuppressLint("RestrictedApi")
    private fun Content(
        model: TodayWidgetModel?,
        cover: Bitmap?,
        modifier: GlanceModifier = GlanceModifier,
    ) {
        Scaffold(modifier = modifier.clickable(intent = model?.intent), horizontalPadding = 0.dp) {
            Box(modifier = GlanceModifier) {
                cover?.let { bitmap ->
                    Image(
                        provider = BitmapImageProvider(bitmap),
                        contentDescription = model?.title,
                        contentScale = ContentScale.Crop,
                        modifier = GlanceModifier.fillMaxSize()
                    )
                }

                val (infoModifier: GlanceModifier, textColor: ColorProvider?) = if (cover != null) {
                    GlanceModifier.background(
                        ImageProvider(R.drawable.bg_img_foreground)
                    ) to ColorProvider(Color.White)
                } else {
                    GlanceModifier.background(
                        GlanceTheme.colors.primary
                    ) to ColorProvider(GlanceTheme.colors.onPrimary.getColor(LocalContext.current))
                }

                TodayInfo(
                    spec = TodayInfoSpec(
                        model = model,
                        textColor = textColor,
                        readOptions = if (cover == null) {
                            TodayInfoSpec.ReadOptions.Shown(
                                ButtonDefaults.buttonColors(
                                    backgroundColor = GlanceTheme.colors.onPrimary,
                                    contentColor = GlanceTheme.colors.primary
                                )
                            )
                        } else {
                            TodayInfoSpec.ReadOptions.Hidden
                        }
                    ),
                    modifier = infoModifier,
                )
            }
        }
    }
}
