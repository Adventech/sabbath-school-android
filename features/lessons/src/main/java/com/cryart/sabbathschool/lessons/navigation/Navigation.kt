/*
 * Copyright (c) 2022. Adventech <info@adventech.io>
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

package com.cryart.sabbathschool.lessons.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import app.ss.models.QuarterlyGroup
import com.cryart.sabbathschool.lessons.ui.quarterlies.QuarterliesRoute
import com.cryart.sabbathschool.lessons.ui.quarterlies.components.QuarterliesGroupCallback
import com.google.accompanist.navigation.animation.composable

const val sabbathSchoolRoute = "route_sabbath_school"

fun NavController.navigateToSabbathSchool(navOptions: NavOptions? = null) {
    navigate(sabbathSchoolRoute, navOptions)
}

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.sabbathSchoolScreen(
    mainPadding: PaddingValues
) {
    composable(
        route = sabbathSchoolRoute,
    ) {
        QuarterliesRoute(
            callbacks = object : QuarterliesGroupCallback {
                override fun onSeeAllClick(group: QuarterlyGroup) {
                }

                override fun profileClick() {
                }

                override fun filterLanguages() {
                }

                override fun onReadClick(index: String) {
                }
            },
            mainPadding = mainPadding
        )
    }
}
