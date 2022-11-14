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

package com.cryart.sabbathschool.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navOptions
import com.cryart.sabbathschool.lessons.navigation.navigateToSabbathSchoolGraph
import com.cryart.sabbathschool.navigation.TopLevelDestination
import com.cryart.sabbathschool.navigation.TopLevelDestination.ACCOUNT
import com.cryart.sabbathschool.navigation.TopLevelDestination.DEVOTIONAL
import com.cryart.sabbathschool.navigation.TopLevelDestination.PERSONAL_MINISTRIES
import com.cryart.sabbathschool.navigation.TopLevelDestination.SABBATH_SCHOOL
import com.cryart.sabbathschool.navigation.navigateToAccount
import com.cryart.sabbathschool.navigation.navigateToDevotional
import com.cryart.sabbathschool.navigation.navigateToPersonalMinistries
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.navigation.material.BottomSheetNavigator
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialNavigationApi::class)
@Composable
fun rememberSsAppState(
    windowSizeClass: WindowSizeClass,
    bottomSheetNavigator: BottomSheetNavigator = rememberBottomSheetNavigator(),
    navController: NavHostController = rememberAnimatedNavController(
        bottomSheetNavigator
    ),
): SsAppState {
    return remember(navController, bottomSheetNavigator, windowSizeClass) {
        SsAppState(navController, bottomSheetNavigator, windowSizeClass)
    }
}

@Immutable
data class SsAppState @OptIn(ExperimentalMaterialNavigationApi::class) constructor(
    val navController: NavHostController,
    val bottomSheetNavigator: BottomSheetNavigator,
    val windowSizeClass: WindowSizeClass,
) {
    val currentDestination: NavDestination?
        @Composable get() = navController
            .currentBackStackEntryAsState().value?.destination

    val shouldShowBottomBar: Boolean
        get() = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact ||
            windowSizeClass.heightSizeClass == WindowHeightSizeClass.Compact

    val shouldShowNavRail: Boolean
        get() = !shouldShowBottomBar

    /**
     * Map of top level destinations to be used in the TopBar, BottomBar and NavRail.
     * The key is the route.
     */
    val topLevelDestinations: List<TopLevelDestination> = TopLevelDestination.values().asList()

    fun navigateToTopLevelDestination(topLevelDestination: TopLevelDestination) {
        val topLevelNavOptions = navOptions {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }

        when (topLevelDestination) {
            SABBATH_SCHOOL -> navController.navigateToSabbathSchoolGraph(topLevelNavOptions)
            PERSONAL_MINISTRIES -> navController.navigateToPersonalMinistries(topLevelNavOptions)
            DEVOTIONAL -> navController.navigateToDevotional(topLevelNavOptions)
            ACCOUNT -> navController.navigateToAccount(topLevelNavOptions)
        }
    }

    fun onBackClick() {
        navController.popBackStack()
    }
}
