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

package app.ss.readings.components

import android.graphics.Color
import android.view.View
import androidx.core.view.iterator
import androidx.lifecycle.LifecycleOwner
import app.ss.readings.SSReadingViewModel
import app.ss.readings.databinding.SsReadingAppBarBinding
import app.ss.readings.model.ReadingsState
import com.cryart.sabbathschool.core.extensions.context.isDarkTheme
import com.cryart.sabbathschool.core.extensions.view.tint
import ss.foundation.coroutines.flow.collectIn
import ss.prefs.api.SSPrefs
import ss.prefs.model.SSReadingDisplayOptions
import ss.prefs.model.colorTheme
import app.ss.readings.R as LessonsR

class AppBarComponent(
    private val binding: SsReadingAppBarBinding,
    viewModel: SSReadingViewModel,
    ssPrefs: SSPrefs,
    owner: LifecycleOwner,
) {
    private val visibleMenuIds = listOf(
        LessonsR.id.ss_reading_menu_audio,
        LessonsR.id.ss_reading_menu_video,
        LessonsR.id.overflow,
    )

    private var isLifted = false
    private lateinit var displayOptions: SSReadingDisplayOptions

    init {
        viewModel.viewState.collectIn(owner) { state ->
            val visibility = when (state) {
                is ReadingsState.Error,
                ReadingsState.Loading -> View.INVISIBLE

                is ReadingsState.Success -> View.VISIBLE
            }
            binding.root.visibility = visibility
        }

        ssPrefs.displayOptionsFlow().collectIn(owner) { options ->
            displayOptions = options
            val color = options.colorTheme(isDarkTheme())
            binding.ssReadingCollapsingToolbar.run {
                setContentScrimColor(color)
                setBackgroundColor(color)
                setStatusBarScrimColor(color)
                setCollapsedTitleTextColor(options.collapsedContentColor())
            }
        }

        binding.ssReadingAppBarLayout.addOnOffsetChangedListener { appBarLayout, _ ->
            if (appBarLayout.isLifted != isLifted) {
                updateColors(appBarLayout.isLifted)
            }
        }
    }

    private fun updateColors(isLifted: Boolean) {
        this.isLifted = isLifted
        val color = displayOptions.collapsedContentColor()
        binding.ssReadingToolbar.run {
            navigationIcon?.tint(color)
            menu.iterator()
                .asSequence()
                .filter { visibleMenuIds.contains(it.itemId) }
                .forEach { it.icon?.tint(color) }
        }
        binding.ssReadingCollapsingToolbar.setCollapsedTitleTextColor(color)
    }

    private fun SSReadingDisplayOptions.collapsedContentColor(): Int {
        return when (theme) {
            SSReadingDisplayOptions.SS_THEME_LIGHT, SSReadingDisplayOptions.SS_THEME_SEPIA -> if (isLifted) Color.BLACK else Color.WHITE
            SSReadingDisplayOptions.SS_THEME_DARK -> Color.WHITE
            else -> if (isLifted) {
                if (isDarkTheme()) Color.WHITE else Color.BLACK
            } else {
                Color.WHITE
            }
        }
    }

    private fun isDarkTheme() = binding.root.context.isDarkTheme()
}
