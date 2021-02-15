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
package com.cryart.sabbathschool.lessons.ui.quarterlies

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.databinding.BaseObservable
import androidx.databinding.BindingAdapter
import coil.load
import com.cryart.sabbathschool.core.extensions.view.tint
import com.cryart.sabbathschool.core.misc.SSConstants
import com.cryart.sabbathschool.lessons.R
import com.cryart.sabbathschool.lessons.data.model.SSQuarterly
import com.cryart.sabbathschool.lessons.ui.lessons.SSLessonsActivity
import com.cryart.sabbathschool.lessons.ui.viewmodel.SSViewModel
import com.google.android.material.button.MaterialButton

internal class SSQuarterlyItemViewModel(
    private var ssQuarterly: SSQuarterly
) : BaseObservable(), SSViewModel {

    fun setSsQuarterly(ssQuarterly: SSQuarterly) {
        this.ssQuarterly = ssQuarterly
        notifyChange()
    }

    val title: String get() = ssQuarterly.title
    val date: String get() = ssQuarterly.human_date
    val cover: String get() = ssQuarterly.cover
    val description: String get() = ssQuarterly.description
    val colorPrimary: Int get() = Color.parseColor(ssQuarterly.color_primary)
    val colorPrimaryDark: String get() = ssQuarterly.color_primary_dark

    fun onReadClick(view: View) {
        val context = view.context
        val ssLessonsIntent = Intent(context, SSLessonsActivity::class.java)
        ssLessonsIntent.putExtra(SSConstants.SS_QUARTERLY_INDEX_EXTRA, ssQuarterly.index)
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
            (context as AppCompatActivity),
            view,
            context.getString(R.string.ss_quarterly_cover_transition)
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            context.startActivity(ssLessonsIntent, options.toBundle())
        } else {
            context.startActivity(ssLessonsIntent)
        }
    }

    override fun destroy() {}

    companion object {
        @JvmStatic
        @BindingAdapter("fbDefaultColor")
        fun setFbDefaultColor(view: MaterialButton, color: String?) {
            view.backgroundTintList = ColorStateList.valueOf(Color.parseColor(color))
        }

        @JvmStatic
        @BindingAdapter(value = ["coverUrl", "primaryColor"])
        fun loadCover(view: ImageView, coverUrl: String?, primaryColor: Int?) {
            coverUrl?.let { url ->
                view.load(url) {
                    allowHardware(Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
                    crossfade(true)
                    primaryColor?.let {
                        val drawable = placeholderDrawable(view.context, it)
                        placeholder(drawable)
                        error(drawable)
                    }
                }
            }
        }

        private fun placeholderDrawable(context: Context, color: Int): Drawable? {
            val drawable = ContextCompat.getDrawable(context, R.drawable.bg_cover)
            drawable?.tint(color)
            return drawable
        }
    }
}
