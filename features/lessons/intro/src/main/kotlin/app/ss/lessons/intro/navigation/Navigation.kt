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

package app.ss.lessons.intro.navigation

import android.net.Uri
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import app.ss.lessons.intro.LessonIntroRoute
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.bottomSheet
import ss.misc.SSConstants

private const val lessonIntroRoute = "lesson_intro_route"
private const val indexExtra = SSConstants.SS_QUARTERLY_INDEX_EXTRA

@OptIn(ExperimentalMaterialNavigationApi::class)
fun NavGraphBuilder.lessonIntro() {
    bottomSheet(
        route = "$lessonIntroRoute/{$indexExtra}",
        arguments = listOf(
            navArgument(indexExtra) { type = NavType.StringType },
        ),
    ) {
        LessonIntroRoute()
    }
}

fun NavController.navigateToLessonIntro(index: String) {
    val encodedIndex = Uri.encode(index)
    navigate(route = "$lessonIntroRoute/$encodedIndex")
}
