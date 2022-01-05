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
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.updateAll
import app.ss.widgets.glance.BaseGlanceAppWidgetReceiver
import coil.imageLoader
import coil.request.ImageRequest
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
internal class TodayImageAppWidgetReceiver : BaseGlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget get() = TodayImageAppWidget()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        launch {
            val model = widgetDataProvider.getTodayModel()
            val bitmap = fetchImage(context, model?.cover)

            TodayImageAppWidget(model, bitmap).updateAll(context)
        }
    }

    private suspend fun fetchImage(context: Context, cover: String?): Bitmap? {
        val request = ImageRequest.Builder(context)
            .data(cover)
            .build()

        val drawable = context.imageLoader.execute(request).drawable

        return (drawable as? BitmapDrawable)?.bitmap
    }
}
