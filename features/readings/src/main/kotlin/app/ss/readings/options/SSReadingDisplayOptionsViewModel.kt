/*
 * Copyright (c) 2021 Adventech <info@adventech.io>
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
package app.ss.readings.options

import androidx.core.view.children
import app.ss.readings.databinding.SsReadingDisplayOptionsBinding
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import ss.prefs.api.SSPrefs
import ss.prefs.model.SSReadingDisplayOptions

class SSReadingDisplayOptionsViewModel @AssistedInject constructor(
    private val ssPrefs: SSPrefs,
    @Assisted private val binding: SsReadingDisplayOptionsBinding,
) {

    @AssistedFactory
    interface Factory {
        fun create(binding: SsReadingDisplayOptionsBinding): SSReadingDisplayOptionsViewModel
    }

    private var ssReadingDisplayOptions: SSReadingDisplayOptions? = null

    init {
        ssPrefs.getDisplayOptions { options -> updateWidget(options) }

        binding.ssReadingMenuDisplayOptionsSize.addOnChangeListener { _, value, _ ->
            when (value) {
                0f -> setSize(SSReadingDisplayOptions.SS_SIZE_TINY)
                1f -> setSize(SSReadingDisplayOptions.SS_SIZE_SMALL)
                3f -> setSize(SSReadingDisplayOptions.SS_SIZE_LARGE)
                4f -> setSize(SSReadingDisplayOptions.SS_SIZE_HUGE)
                else -> setSize(SSReadingDisplayOptions.SS_SIZE_MEDIUM)
            }
        }
        binding.ssReadingMenuDisplayOptionsSize.setLabelFormatter { value ->
            when (value) {
                0f -> "XS"
                1f -> "S"
                2f -> "M"
                3f -> "L"
                4f -> "XL"
                else -> ""
            }
        }

        bindClickListeners()
    }

    private fun bindClickListeners() {
        with(binding) {
            themeLight.setOnClickListener { setTheme(SSReadingDisplayOptions.SS_THEME_LIGHT) }
            themeSepia.setOnClickListener { setTheme(SSReadingDisplayOptions.SS_THEME_SEPIA) }
            themeDark.setOnClickListener { setTheme(SSReadingDisplayOptions.SS_THEME_DARK) }
            themeDefault.setOnClickListener { setTheme(SSReadingDisplayOptions.SS_THEME_DEFAULT) }

            fontAndada.setOnClickListener { setFont(SSReadingDisplayOptions.SS_FONT_ANDADA) }
            fontLato.setOnClickListener { setFont(SSReadingDisplayOptions.SS_FONT_LATO) }
            fontSerif.setOnClickListener { setFont(SSReadingDisplayOptions.SS_FONT_PT_SERIF) }
            fontSans.setOnClickListener { setFont(SSReadingDisplayOptions.SS_FONT_PT_SANS) }
        }
    }

    private fun updateWidget(ssReadingDisplayOptions: SSReadingDisplayOptions) {
        this.ssReadingDisplayOptions = ssReadingDisplayOptions

        when (ssReadingDisplayOptions.size) {
            SSReadingDisplayOptions.SS_SIZE_TINY -> {
                binding.ssReadingMenuDisplayOptionsSize.value = 0.0f
            }

            SSReadingDisplayOptions.SS_SIZE_SMALL -> {
                binding.ssReadingMenuDisplayOptionsSize.value = 1f
            }

            SSReadingDisplayOptions.SS_SIZE_MEDIUM -> {
                binding.ssReadingMenuDisplayOptionsSize.value = 2f
            }

            SSReadingDisplayOptions.SS_SIZE_LARGE -> {
                binding.ssReadingMenuDisplayOptionsSize.value = 3f
            }

            SSReadingDisplayOptions.SS_SIZE_HUGE -> {
                binding.ssReadingMenuDisplayOptionsSize.value = 4f
            }
        }

        updateThemeDisplay(ssReadingDisplayOptions.theme)
        updateFontDisplay(ssReadingDisplayOptions.font)
    }

    private fun setTheme(theme: String) {
        ssReadingDisplayOptions?.theme = theme
        relaySSReadingDisplayOptions()

        updateThemeDisplay(theme)
    }

    private fun updateThemeDisplay(theme: String) {
        val selectedView = when (theme) {
            SSReadingDisplayOptions.SS_THEME_LIGHT -> binding.themeLight
            SSReadingDisplayOptions.SS_THEME_DARK -> binding.themeDark
            SSReadingDisplayOptions.SS_THEME_SEPIA -> binding.themeSepia
            SSReadingDisplayOptions.SS_THEME_DEFAULT -> binding.themeDefault
            else -> return
        }
        binding.themeContainer.children.forEach { view ->
            view.isSelected = view == selectedView
        }
    }

    private fun setSize(size: String) {
        ssReadingDisplayOptions?.size = size
        relaySSReadingDisplayOptions()
    }

    private fun setFont(font: String) {
        ssReadingDisplayOptions?.font = font
        relaySSReadingDisplayOptions()

        updateFontDisplay(font)
    }

    private fun updateFontDisplay(font: String) {
        val selectedView = when (font) {
            SSReadingDisplayOptions.SS_FONT_ANDADA -> binding.fontAndada
            SSReadingDisplayOptions.SS_FONT_LATO -> binding.fontLato
            SSReadingDisplayOptions.SS_FONT_PT_SERIF -> binding.fontSerif
            SSReadingDisplayOptions.SS_FONT_PT_SANS -> binding.fontSans
            else -> return
        }
        binding.fontContainer.children.forEach { view ->
            view.isSelected = view == selectedView
        }
    }

    private fun relaySSReadingDisplayOptions() {
        ssReadingDisplayOptions?.let { options -> ssPrefs.setDisplayOptions(options) }
    }
}
