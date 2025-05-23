/*
 * Copyright (c) 2025. Adventech <info@adventech.io>
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

package com.cryart.sabbathschool.navigation

import android.content.Context
import android.content.Intent
import android.os.Build
import app.ss.widgets.AppWidgetAction
import com.cryart.sabbathschool.core.navigation.AppNavigator
import dagger.hilt.android.qualifiers.ApplicationContext
import ss.libraries.circuit.navigation.DocumentScreen
import ss.libraries.circuit.navigation.ResourceScreen
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppWidgetActionImpl @Inject constructor(
    @param:ApplicationContext private val appContext: Context,
    private val appNavigator: AppNavigator,
) : AppWidgetAction {

    override fun launchLesson(quarterlyIndex: String): Intent =
        appNavigator.screenIntent(
            appContext, ResourceScreen(quarterlyIndex.toResourceIndex())
        )

    override fun launchRead(lessonIndex: String, dayIndex: String?): Intent =
        appNavigator.screenIntent(
            appContext,
            DocumentScreen(index = lessonIndex.toDocumentIndex(), dayIndex)
        ).apply {
            // See [Intent.filterEquals].
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                identifier = "$lessonIndex/${dayIndex.orEmpty()}"
            }
        }
}
