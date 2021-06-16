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

import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.core.view.children
import com.cryart.sabbathschool.core.extensions.prefs.SSPrefs
import com.cryart.sabbathschool.core.model.SSReadingDisplayOptions
import com.cryart.sabbathschool.lessons.databinding.SsReadingDisplayOptionsBinding

class SSReadingDisplayOptionsViewModel(
    private val binding: SsReadingDisplayOptionsBinding,
    private val ssPrefs: SSPrefs
) {

    private var ssReadingDisplayOptions: SSReadingDisplayOptions? = null

    init {
        ssPrefs.getDisplayOptions { options -> updateWidget(options) }

        binding.ssReadingMenuDisplayOptionsSize.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                when (i) {
                    0 -> {
                        setSizeTiny()
                    }
                    1 -> {
                        setSizeSmall()
                    }
                    3 -> {
                        setSizeLarge()
                    }
                    4 -> {
                        setSizeHuge()
                    }
                    else -> {
                        setSizeMedium()
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    private fun updateWidget(ssReadingDisplayOptions: SSReadingDisplayOptions) {
        this.ssReadingDisplayOptions = ssReadingDisplayOptions

        when (ssReadingDisplayOptions.size) {
            SSReadingDisplayOptions.SS_SIZE_TINY -> {
                binding.ssReadingMenuDisplayOptionsSize.progress = 0
            }
            SSReadingDisplayOptions.SS_SIZE_SMALL -> {
                binding.ssReadingMenuDisplayOptionsSize.progress = 1
            }
            SSReadingDisplayOptions.SS_SIZE_MEDIUM -> {
                binding.ssReadingMenuDisplayOptionsSize.progress = 2
            }
            SSReadingDisplayOptions.SS_SIZE_LARGE -> {
                binding.ssReadingMenuDisplayOptionsSize.progress = 3
            }
            SSReadingDisplayOptions.SS_SIZE_HUGE -> {
                binding.ssReadingMenuDisplayOptionsSize.progress = 4
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
