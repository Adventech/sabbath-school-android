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

package com.cryart.sabbathschool.navigation

import android.content.Context
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.transitions.scaleInEnterTransition
import app.ss.design.compose.transitions.scaleInPopEnterTransition
import app.ss.design.compose.transitions.scaleOutExitTransition
import app.ss.design.compose.transitions.scaleOutPopExitTransition
import app.ss.languages.navigation.languagesScreen
import app.ss.languages.navigation.navigateToLanguages
import app.ss.lessons.intro.navigation.lessonIntro
import app.ss.lessons.intro.navigation.navigateToLessonIntro
import com.cryart.sabbathschool.lessons.navigation.lessonsScreen
import com.cryart.sabbathschool.lessons.navigation.navigateToLessons
import com.cryart.sabbathschool.lessons.navigation.sabbathSchoolGraph
import com.cryart.sabbathschool.lessons.navigation.sabbathSchoolRoutePattern
import com.cryart.sabbathschool.lessons.ui.readings.SSReadingActivity
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable

/**
 * Temporary routes:
 * TODO: Move to feature modules
 */
private const val personalMinistriesRoute = "route_personal_ministries"
private const val devotionalRoute = "route_devotional"
private const val accountRoute = "route_account"

fun NavController.navigateToPersonalMinistries(navOptions: NavOptions? = null) {
    navigate(personalMinistriesRoute, navOptions)
}

fun NavController.navigateToDevotional(navOptions: NavOptions? = null) {
    navigate(devotionalRoute, navOptions)
}

fun NavController.navigateToAccount(navOptions: NavOptions? = null) {
    navigate(accountRoute, navOptions)
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun SsNavHost(
    navController: NavHostController,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    startDestination: String = sabbathSchoolRoutePattern,
    context: Context = LocalContext.current
) {
    val bottomPadding = 80.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val mainPadding = PaddingValues(bottom = bottomPadding)

    AnimatedNavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        sabbathSchoolGraph(
            navigateToLanguages = { navController.navigateToLanguages() },
            navigateToLesson = { navController.navigateToLessons(it) },
            mainPadding = mainPadding,
            nestedGraphs = {
                languagesScreen(
                    mainPadding = mainPadding,
                    onBackClick = onBackClick
                )

                lessonsScreen(
                    mainPadding = mainPadding,
                    readLesson = { index ->
                        val readIntent = SSReadingActivity.launchIntent(context, index)
                        context.startActivity(readIntent)
                    },
                    lessonIntro = { index ->
                        navController.navigateToLessonIntro(index)
                    },
                    onBackClick = onBackClick
                )

                lessonIntro()
            }
        )
        sampleScreen(personalMinistriesRoute)
        sampleScreen(devotionalRoute)
        sampleScreen(accountRoute)
    }
}

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.sampleScreen(route: String) {
    composable(
        route = route,
        enterTransition = { scaleInEnterTransition() },
        exitTransition = { scaleOutExitTransition() },
        popEnterTransition = { scaleInPopEnterTransition() },
        popExitTransition = { scaleOutPopExitTransition() }
    ) {
        SampleScreen(title = route)
    }
}

@Composable
private fun SampleScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text = title,
            style = SsTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
