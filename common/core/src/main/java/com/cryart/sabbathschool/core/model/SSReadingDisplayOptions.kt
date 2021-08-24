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
package com.cryart.sabbathschool.core.model

import android.graphics.Color

data class SSReadingDisplayOptions @JvmOverloads constructor(
    var theme: String = SS_THEME_LIGHT,
    var size: String = SS_SIZE_MEDIUM,
    var font: String = SS_FONT_ANDADA
) {

    val themeColor: String
        get() {
            return when (theme) {
                SS_THEME_LIGHT -> SS_THEME_LIGHT_HEX
                SS_THEME_DARK -> SS_THEME_DARK_HEX
                SS_THEME_SEPIA -> SS_THEME_SEPIA_HEX
                else -> ""
            }
        }

    val colorTheme: Int get() = Color.parseColor(themeColor)

    companion object {
        const val SS_THEME_LIGHT = "light"
        const val SS_THEME_SEPIA = "sepia"
        const val SS_THEME_DARK = "dark"
        const val SS_THEME_LIGHT_HEX = "#ffffff"
        const val SS_THEME_SEPIA_HEX = "#fbf0d9"
        const val SS_THEME_DARK_HEX = "#000000"
        const val SS_SIZE_TINY = "tiny"
        const val SS_SIZE_SMALL = "small"
        const val SS_SIZE_MEDIUM = "medium"
        const val SS_SIZE_LARGE = "large"
        const val SS_SIZE_HUGE = "huge"
        const val SS_FONT_ANDADA = "andada"
        const val SS_FONT_LATO = "lato"
        const val SS_FONT_PT_SERIF = "pt-serif"
        const val SS_FONT_PT_SANS = "pt-sans"

        fun create(isDark: Boolean = true): SSReadingDisplayOptions {
            return SSReadingDisplayOptions(
                if (isDark) SS_THEME_DARK else SS_THEME_LIGHT,
            )
        }
    }
}
