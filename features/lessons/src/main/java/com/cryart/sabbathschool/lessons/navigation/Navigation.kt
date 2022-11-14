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

import android.net.Uri
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navigation
import app.ss.design.compose.transitions.scaleInEnterTransition
import app.ss.design.compose.transitions.scaleOutPopExitTransition
import app.ss.design.compose.transitions.slideInTransition
import app.ss.design.compose.transitions.slideOutTransition
import app.ss.models.LessonIntroModel
import app.ss.models.QuarterlyGroup
import com.cryart.sabbathschool.core.misc.SSConstants.SS_QUARTERLY_INDEX_EXTRA
import com.cryart.sabbathschool.lessons.ui.lessons.LessonsRoute
import com.cryart.sabbathschool.lessons.ui.quarterlies.QuarterliesRoute
import com.cryart.sabbathschool.lessons.ui.quarterlies.components.QuarterliesGroupCallback
import com.google.accompanist.navigation.animation.composable

fun NavController.navigateToSabbathSchoolGraph(navOptions: NavOptions? = null) {
    navigate(sabbathSchoolRoutePattern, navOptions)
}

const val sabbathSchoolRoutePattern = "sabbath_school_graph"
private const val quarterliesRoute = "quarterlies_route"

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.sabbathSchoolGraph(
    navigateToLanguages: () -> Unit,
    navigateToLesson: (String) -> Unit,
    mainPadding: PaddingValues,
    nestedGraphs: NavGraphBuilder.() -> Unit
) {
    navigation(
        route = sabbathSchoolRoutePattern,
        startDestination = quarterliesRoute
    ) {
        composable(
            route = quarterliesRoute,
            enterTransition = { scaleInEnterTransition() },
            exitTransition = { fadeOut() },
            popEnterTransition = { fadeIn() },
            popExitTransition = { scaleOutPopExitTransition() }
        ) {
            QuarterliesRoute(
                callbacks = object : QuarterliesGroupCallback {
                    override fun onSeeAllClick(group: QuarterlyGroup) {
                        // TODO: Show Quarterly List
                    }

                    override fun profileClick() {
                        // TODO: Show Profile
                    }

                    override fun filterLanguages() {
                        navigateToLanguages()
                    }

                    override fun onReadClick(index: String) {
                        navigateToLesson(index)
                    }
                },
                mainPadding = mainPadding
            )
        }
        nestedGraphs()
    }
}

// Lessons
private const val lessonsRoute = "lessons_route"
internal const val lessonIndexArg = SS_QUARTERLY_INDEX_EXTRA

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.lessonsScreen(
    mainPadding: PaddingValues,
    readLesson: (String) -> Unit,
    lessonIntro: (LessonIntroModel) -> Unit,
    onBackClick: () -> Unit,
) {
    composable(
        route = "$lessonsRoute/{$lessonIndexArg}",
        arguments = listOf(
            navArgument(lessonIndexArg) { type = NavType.StringType }
        ),
        enterTransition = { slideInTransition() },
        popEnterTransition = { fadeIn() },
        popExitTransition = { slideOutTransition() }
    ) {
        LessonsRoute(
            readLesson = readLesson,
            lessonIntro = lessonIntro,
            onNavClick = onBackClick,
            mainPadding = mainPadding,
        )
    }
}

fun NavController.navigateToLessons(index: String) {
    val encodedIndex = Uri.encode(index)
    navigate("$lessonsRoute/$encodedIndex")
}
