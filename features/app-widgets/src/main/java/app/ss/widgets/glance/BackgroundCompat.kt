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

package app.ss.widgets.glance

import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.glance.BitmapImageProvider
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import android.graphics.Color as AndroidColor

/**
 * Adds rounded corners for the current view.
 *
 * On S+ it uses [GlanceModifier.cornerRadius]
 * on <S it creates [ShapeDrawable] and sets background
 *
 * @param cornerRadius [Int] radius set to all corners of the view.
 * @param color [Color] value of a color that will be set as background
 */
@RequiresApi(Build.VERSION_CODES.O)
internal fun GlanceModifier.cornerRadiusCompat(
    cornerRadius: Int,
    color: Color,
): GlanceModifier {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        this.background(color)
            .cornerRadius(cornerRadius.dp)
    } else {
        val radii = FloatArray(8) { cornerRadius.toFloat() }
        val shape = ShapeDrawable(RoundRectShape(radii, null, null))
        shape.paint.color = AndroidColor.rgb(color.red, color.green, color.blue)
        this.background(BitmapImageProvider(shape.toBitmap()))
    }
}
