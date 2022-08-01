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

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.BitmapImageProvider
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.SizeMode
import androidx.glance.background
import androidx.glance.layout.Box
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import app.ss.widgets.R
import app.ss.widgets.WidgetDataProvider
import app.ss.widgets.glance.BaseGlanceAppWidget
import app.ss.widgets.glance.extensions.clickable
import app.ss.widgets.glance.extensions.modifyAppWidgetBackground
import app.ss.widgets.glance.theme.SsGlanceTheme
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

    override val sizeMode = SizeMode.Responsive(
        setOf(DpSize(180.dp, 110.dp), DpSize(300.dp, 110.dp))
    )

    override suspend fun loadData(): Data {
        val model = dataProvider.getTodayModel()
        val bitmap = context.fetchBitmap(model?.cover)
        return Data(model, modelCover = bitmap)
    }

    @Composable
    @SuppressLint("RestrictedApi")
    override fun Content(data: Data?) {
        val model = data?.model
        val modelCover = data?.modelCover

        SsGlanceTheme {
            Box(
                modifier = GlanceModifier
                    .modifyAppWidgetBackground()
                    .clickable(uri = model?.uri)
            ) {
                modelCover?.let { bitmap ->
                    Image(
                        provider = BitmapImageProvider(bitmap),
                        contentDescription = model?.title,
                        contentScale = ContentScale.Crop,
                        modifier = GlanceModifier.fillMaxSize()
                    )
                }

                val (modifier: GlanceModifier, textColor: Color?) = if (modelCover != null) {
                    GlanceModifier.background(
                        ImageProvider(R.drawable.bg_img_foreground)
                    ) to Color.White
                } else {
                    GlanceModifier to null
                }

                TodayInfo(
                    infoModel = model,
                    modifier = modifier,
                    textColor = textColor
                )
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(context: Context): TodayImageAppWidget
    }
}
