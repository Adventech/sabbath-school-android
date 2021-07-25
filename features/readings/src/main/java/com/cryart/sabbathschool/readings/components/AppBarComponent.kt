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

package com.cryart.sabbathschool.readings.components

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LifecycleOwner
import coil.load
import com.cryart.sabbathschool.core.extensions.context.colorPrimary
import com.cryart.sabbathschool.core.extensions.context.colorPrimaryDark
import com.cryart.sabbathschool.core.extensions.coroutines.flow.collectIn
import com.cryart.sabbathschool.core.extensions.view.fadeTo
import com.cryart.sabbathschool.core.extensions.view.tint
import com.cryart.sabbathschool.readings.R
import com.cryart.sabbathschool.readings.components.model.AppBarData
import com.cryart.sabbathschool.readings.databinding.ComponentReadingAppBarBinding
import kotlinx.coroutines.flow.Flow

class AppBarComponent(
    lifecycleOwner: LifecycleOwner,
    private val binding: ComponentReadingAppBarBinding
) : BaseComponent<AppBarData>(lifecycleOwner) {

    init {
        binding.collapsingToolbar.apply {
            setBackgroundColor(context.colorPrimary)
            setContentScrimColor(context.colorPrimary)
            setStatusBarScrimColor(context.colorPrimaryDark)
            setCollapsedTitleTypeface(ResourcesCompat.getFont(context, R.font.lato_bold))
            setExpandedTitleTypeface(ResourcesCompat.getFont(context, R.font.lato_bold))
        }
    }

    private fun placeholderDrawable(context: Context): Drawable? {
        val drawable = ContextCompat.getDrawable(context, R.drawable.bg_cover)
        drawable?.tint(context.colorPrimary)
        return drawable
    }

    override fun collect(visibilityFlow: Flow<Boolean>, dataFlow: Flow<AppBarData>) {
        visibilityFlow.collectIn(owner) { visible ->
            binding.appBar.fadeTo(visible)
        }

        dataFlow.collectIn(owner) { data ->
            when (data) {
                is AppBarData.Cover -> {
                    binding.backdrop.apply {
                        val drawable = placeholderDrawable(context)
                        load(data.image) {
                            placeholder(drawable)
                            error(drawable)
                        }
                    }
                }
                is AppBarData.Title -> {
                    binding.apply {
                        collapsingToolbar.title = data.title
                        subtitle.text = data.subTitle
                    }
                }
                AppBarData.Empty -> {
                }
            }
        }
    }
}
