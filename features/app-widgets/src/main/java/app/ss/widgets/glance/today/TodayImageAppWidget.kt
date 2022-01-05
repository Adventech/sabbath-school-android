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

import android.graphics.Bitmap
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.BitmapImageProvider
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Box
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import app.ss.widgets.glance.theme.SsAppWidgetTheme
import app.ss.widgets.model.TodayWidgetModel

internal class TodayImageAppWidget(
    private val model: TodayWidgetModel? = null,
    private val modelCover: Bitmap? = null
) : GlanceAppWidget() {

    @Composable
    override fun Content() {
        SsAppWidgetTheme {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .appWidgetBackground()
                    .cornerRadius(20.dp)
            ) {
                modelCover?.let { bitmap ->
                    Image(
                        provider = BitmapImageProvider(bitmap),
                        contentDescription = model?.title,
                        contentScale = ContentScale.Crop,
                        modifier = GlanceModifier.fillMaxSize()
                    )
                }

                TodayInfo(model = model ?: errorModel())
            }
        }
    }
}
