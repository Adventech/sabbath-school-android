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

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.glance.BitmapImageProvider
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Box
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import app.ss.widgets.WidgetDataProvider
import app.ss.widgets.glance.BaseGlanceAppWidget
import app.ss.widgets.glance.theme.AppWidgetCornerRadius
import app.ss.widgets.glance.theme.SsAppWidgetTheme
import app.ss.widgets.model.TodayWidgetModel
import com.cryart.sabbathschool.core.extensions.context.fetchBitmap
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

internal class TodayImageAppWidget @AssistedInject constructor(
    private val dataProvider: WidgetDataProvider,
    @Assisted private val context: Context,
) : BaseGlanceAppWidget<TodayImageAppWidget.Data>(context = context) {

    data class Data(
        val model: TodayWidgetModel? = null,
        val modelCover: Bitmap? = null
    )

    override suspend fun loadData(): Data {
        val model = dataProvider.getTodayModel()
        val bitmap = context.fetchBitmap(model?.cover)
        return Data(model, modelCover = bitmap)
    }

    @Composable
    override fun Content(data: Data?) {
        val model = data?.model
        val modelCover = data?.modelCover

        SsAppWidgetTheme {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .appWidgetBackground()
                    .cornerRadius(AppWidgetCornerRadius)
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

    @AssistedFactory
    interface Factory {
        fun create(context: Context): TodayImageAppWidget
    }
}
