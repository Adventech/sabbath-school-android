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

@file:OptIn(ExperimentalMaterial3WindowSizeClassApi::class)

package com.cryart.sabbathschool.lessons.ui.quarterlies.list

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.core.view.WindowCompat
import app.ss.design.compose.theme.SsTheme
import app.ss.models.QuarterlyGroup
import com.cryart.sabbathschool.core.misc.SSConstants
import com.cryart.sabbathschool.core.ui.SlidingActivity
import com.cryart.sabbathschool.lessons.ui.lessons.SSLessonsActivity
import com.cryart.sabbathschool.lessons.ui.quarterlies.QuarterliesScreen
import com.cryart.sabbathschool.lessons.ui.quarterlies.components.QuarterliesListCallback
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QuarterliesListActivity : SlidingActivity(), QuarterliesListCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            SsTheme(
                windowWidthSizeClass = calculateWindowSizeClass(activity = this).widthSizeClass
            ) {
                QuarterliesScreen(callbacks = this)
            }
        }
    }

    override fun backNavClick() {
        finishAfterTransition()
    }

    override fun onReadClick(index: String) {
        val lessonsIntent = SSLessonsActivity.launchIntent(this, index)
        startActivity(lessonsIntent)
    }

    companion object {
        fun launchIntent(
            context: Context,
            group: QuarterlyGroup
        ): Intent = Intent(
            context,
            QuarterliesListActivity::class.java
        ).apply {
            putExtra(SSConstants.SS_QUARTERLY_GROUP, group)
        }
    }
}
