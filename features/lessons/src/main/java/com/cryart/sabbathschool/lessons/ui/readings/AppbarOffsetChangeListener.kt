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

package com.cryart.sabbathschool.lessons.ui.readings

import android.app.Activity
import android.graphics.Color
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import com.cryart.sabbathschool.core.extensions.activity.setLightStatusBar
import com.cryart.sabbathschool.core.extensions.view.tint
import app.ss.models.SSReadingDisplayOptions
import app.ss.models.displayTheme
import com.cryart.sabbathschool.core.extensions.context.isDarkTheme
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout

/**
 * Adding this OffsetChangeListener breaks AppBarLayout scrolling on Samsung devices
 * specifically Note 9 with Android 8.1.
 */
class AppbarOffsetChangeListener(
    private val activity: Activity,
    private val collapsingToolbar: CollapsingToolbarLayout,
    private val toolbar: Toolbar
) : AppBarLayout.OnOffsetChangedListener {

    private var isCollapsed: Boolean = false

    var readingOptions: SSReadingDisplayOptions? = null
        set(value) {
            field = value
            themeContent(isCollapsed)
        }

    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
        val collapsed = collapsingToolbar.height + verticalOffset < 2 * ViewCompat.getMinimumHeight(collapsingToolbar)
        if (isCollapsed == collapsed) {
            return
        }
        isCollapsed = collapsed
        themeContent(collapsed)
    }

    private fun themeContent(collapsed: Boolean) {
        val color = when {
            readingOptions?.displayTheme(activity.isDarkTheme()) == SSReadingDisplayOptions.SS_THEME_DARK || !collapsed -> Color.WHITE
            else -> Color.BLACK
        }

        if (readingOptions?.displayTheme(activity.isDarkTheme()) != SSReadingDisplayOptions.SS_THEME_DARK) {
            activity.setLightStatusBar(collapsed)
        } else {
            activity.setLightStatusBar(false)
        }

        toolbar.navigationIcon?.tint(color)
        toolbar.overflowIcon?.tint(color)

        val size = toolbar.menu.size()
        for (i in 0 until size) {
            val item = toolbar.menu.getItem(i)
            item.icon?.tint(color)
        }
    }
}
