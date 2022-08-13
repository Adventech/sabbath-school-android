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

package app.ss.pdf

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.pspdfkit.configuration.PdfConfiguration
import com.pspdfkit.configuration.page.PageLayoutMode
import com.pspdfkit.configuration.page.PageScrollDirection
import com.pspdfkit.configuration.page.PageScrollMode
import com.pspdfkit.configuration.theming.ThemeMode

interface PdfReaderPrefs {
    fun scrollMode(): PageScrollMode
    fun setScrollMode(mode: PageScrollMode)
    fun pageLayoutMode(): PageLayoutMode
    fun setPageLayoutMode(mode: PageLayoutMode)
    fun scrollDirection(): PageScrollDirection
    fun setScrollDirection(direction: PageScrollDirection)
    fun themeMode(): ThemeMode
    fun setThemeMode(mode: ThemeMode)
    fun saveConfiguration(configuration: PdfConfiguration)
}

internal class PdfReaderPrefsImpl(
    private val context: Context,
    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
) : PdfReaderPrefs {

    override fun scrollMode(): PageScrollMode =
        sharedPreferences.getString(KEY_SCROLL_MODE, null)?.let {
            PageScrollMode.valueOf(it)
        } ?: PageScrollMode.CONTINUOUS

    override fun setScrollMode(mode: PageScrollMode) {
        sharedPreferences.edit {
            putString(KEY_SCROLL_MODE, mode.name)
        }
    }

    override fun pageLayoutMode(): PageLayoutMode =
        sharedPreferences.getString(KEY_LAYOUT_MODE, null)?.let {
            PageLayoutMode.valueOf(it)
        } ?: PageLayoutMode.SINGLE

    override fun setPageLayoutMode(mode: PageLayoutMode) {
        sharedPreferences.edit {
            putString(KEY_LAYOUT_MODE, mode.name)
        }
    }

    override fun scrollDirection(): PageScrollDirection =
        sharedPreferences.getString(KEY_SCROLL_DIRECTION, null)?.let {
            PageScrollDirection.valueOf(it)
        } ?: PageScrollDirection.VERTICAL

    override fun setScrollDirection(direction: PageScrollDirection) {
        sharedPreferences.edit {
            putString(KEY_SCROLL_DIRECTION, direction.name)
        }
    }

    override fun themeMode(): ThemeMode =
        sharedPreferences.getString(KEY_THEME_MODE, null)?.let {
            ThemeMode.valueOf(it)
        } ?: ThemeMode.DEFAULT

    override fun setThemeMode(mode: ThemeMode) {
        sharedPreferences.edit {
            putString(KEY_THEME_MODE, mode.name)
        }
    }

    override fun saveConfiguration(configuration: PdfConfiguration) {
        setScrollMode(configuration.scrollMode)
        setPageLayoutMode(configuration.layoutMode)
        setScrollDirection(configuration.scrollDirection)
        setThemeMode(configuration.themeMode)
    }

    companion object {
        private const val KEY_LAYOUT_MODE = "key:layout_mode"
        private const val KEY_SCROLL_MODE = "key:scroll_mode"
        private const val KEY_SCROLL_DIRECTION = "key:scroll_direction"
        private const val KEY_THEME_MODE = "key:theme_mode"
    }
}
