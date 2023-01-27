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
package com.cryart.sabbathschool.lessons.ui.readings.options

import androidx.core.view.children
import com.cryart.sabbathschool.lessons.databinding.SsReadingDisplayOptionsBinding
import ss.prefs.api.SSPrefs
import ss.prefs.model.SSReadingDisplayOptions

class SSReadingDisplayOptionsViewModel(
    private val binding: SsReadingDisplayOptionsBinding,
    private val ssPrefs: SSPrefs
) {

    private var ssReadingDisplayOptions: SSReadingDisplayOptions? = null

    init {
        ssPrefs.getDisplayOptions { options -> updateWidget(options) }

        binding.ssReadingMenuDisplayOptionsSize.addOnChangeListener { _, value, _ ->
            when (value) {
                0f -> {
                    setSizeTiny()
                }
                1f -> {
                    setSizeSmall()
                }
                3f -> {
                    setSizeLarge()
                }
                4f -> {
                    setSizeHuge()
                }
                else -> {
                    setSizeMedium()
                }
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

    fun setFontAndada() {
        setFont(SSReadingDisplayOptions.SS_FONT_ANDADA)
    }

    fun setFontLato() {
        setFont(SSReadingDisplayOptions.SS_FONT_LATO)
    }

    fun setFontPTSerif() {
        setFont(SSReadingDisplayOptions.SS_FONT_PT_SERIF)
    }

    fun setFontPTSans() {
        setFont(SSReadingDisplayOptions.SS_FONT_PT_SANS)
    }

    fun setThemeLight() {
        setTheme(SSReadingDisplayOptions.SS_THEME_LIGHT)
    }

    fun setThemeSepia() {
        setTheme(SSReadingDisplayOptions.SS_THEME_SEPIA)
    }

    fun setThemeDark() {
        setTheme(SSReadingDisplayOptions.SS_THEME_DARK)
    }

    fun setThemeDefault() {
        setTheme(SSReadingDisplayOptions.SS_THEME_DEFAULT)
    }

    fun setSizeTiny() {
        setSize(SSReadingDisplayOptions.SS_SIZE_TINY)
    }

    fun setSizeSmall() {
        setSize(SSReadingDisplayOptions.SS_SIZE_SMALL)
    }

    fun setSizeMedium() {
        setSize(SSReadingDisplayOptions.SS_SIZE_MEDIUM)
    }

    fun setSizeLarge() {
        setSize(SSReadingDisplayOptions.SS_SIZE_LARGE)
    }

    fun setSizeHuge() {
        setSize(SSReadingDisplayOptions.SS_SIZE_HUGE)
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
