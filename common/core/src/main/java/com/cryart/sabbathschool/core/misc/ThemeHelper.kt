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

package com.cryart.sabbathschool.core.misc

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.cryart.sabbathschool.core.R

object ThemeHelper {

    fun setTheme(context: Context, theme: String) {
        val mode = when (theme) {
            context.getString(R.string.ss_settings_theme_value_light) -> AppCompatDelegate.MODE_NIGHT_NO
            context.getString(R.string.ss_settings_theme_value_dark) -> AppCompatDelegate.MODE_NIGHT_YES
            context.getString(R.string.ss_settings_theme_value_default) -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            else -> return
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }
}
