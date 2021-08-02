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

package com.cryart.sabbathschool.lessons.ui.base

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.widget.ImageView
import androidx.core.content.ContextCompat
import coil.load
import com.cryart.sabbathschool.core.extensions.view.tint
import com.cryart.sabbathschool.lessons.R

fun ImageView.loadCover(coverUrl: String?, primaryColor: Int? = null) {
    load(coverUrl) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            allowHardware(true)
            crossfade(true)
        }
        bitmapConfig(Bitmap.Config.ARGB_8888)
        primaryColor?.let {
            val drawable = placeholderDrawable(context, it)
            placeholder(drawable)
            error(drawable)
        }
    }
}

private fun placeholderDrawable(context: Context, color: Int): Drawable? {
    val drawable = ContextCompat.getDrawable(context, R.drawable.bg_cover)
    drawable?.tint(color)
    return drawable
}
