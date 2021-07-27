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

package com.cryart.sabbathschool.lessons.ui.lessons.components

import android.app.Activity
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import androidx.lifecycle.LifecycleOwner
import com.cryart.sabbathschool.core.extensions.activity.setLightStatusBar
import com.cryart.sabbathschool.core.extensions.context.isDarkTheme
import com.cryart.sabbathschool.core.extensions.coroutines.flow.collectIn
import com.cryart.sabbathschool.core.extensions.view.doOnApplyWindowInsets
import com.cryart.sabbathschool.core.ui.BaseDataComponent
import com.cryart.sabbathschool.lessons.R
import com.cryart.sabbathschool.lessons.databinding.SsLessonsToolbarBinding
import kotlinx.coroutines.flow.Flow

class ToolbarComponent constructor(
    lifecycleOwner: LifecycleOwner,
    private val binding: SsLessonsToolbarBinding
) : BaseDataComponent<String?>(lifecycleOwner) {

    init {
        binding.ssLessonsToolbar.apply {
            fitsSystemWindows = false
            doOnApplyWindowInsets { insetView, windowInsets, _, initialMargins ->
                insetView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    updateMargins(top = initialMargins.top + windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).top)
                }
            }
        }
    }

    private val solidColor = ContextCompat.getColor(binding.root.context, R.color.ss_theme_color_background)

    override fun collect(dataFlow: Flow<String?>) {
        dataFlow.collectIn(owner) { title ->
            binding.ssLessonsToolbarTitle.text = title
        }
    }

    fun onContentScroll(scrollY: Int, anchorHeight: Int, activity: Activity) {
        val scrollValue = scrollY.coerceAtLeast(0).toDouble()
        val viewAlpha = (scrollValue / anchorHeight).coerceAtLeast(0.0)
        val colorAlpha = (viewAlpha * MAX_ALPHA).toInt()
        val isSolid = colorAlpha >= MIN_SOLID_ALPHA

        val backgroundColor = ColorUtils.setAlphaComponent(solidColor, colorAlpha.coerceIn(0, MAX_ALPHA))
        with(binding.ssLessonsToolbar) {
            setBackgroundColor(backgroundColor)
            isActivated = isSolid
        }

        binding.ssLessonsToolbarTitle.alpha =
            if (isSolid) {
                viewAlpha.toFloat()
            } else {
                viewAlpha.minus(0.5).toFloat()
            }

        with(activity) {
            window?.statusBarColor = backgroundColor
            if (!isDarkTheme()) {
                setLightStatusBar(isSolid)
            }
        }
    }

    companion object {
        private const val MAX_ALPHA = 255
        private const val MIN_SOLID_ALPHA = 229
    }
}
