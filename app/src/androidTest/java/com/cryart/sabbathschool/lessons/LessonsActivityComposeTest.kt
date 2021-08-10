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

package com.cryart.sabbathschool.lessons

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import app.ss.lessons.data.model.Feature
import com.cryart.design.theme.SSTheme
import com.cryart.sabbathschool.lessons.ui.lessons.components.features.QuarterlyFeaturesRow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class LessonsActivityComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun check_feature_images_are_displayed() {
        composeTestRule.setContent {
            SSTheme {
                QuarterlyFeaturesRow(
                    features = listOf(
                        Feature(
                            name = "",
                            title = "title-1",
                            image = "https://sabbath-school.adventech.io/api/v1/images/features/feature_egw.png"
                        ),
                        Feature(
                            name = "",
                            title = "title-2",
                            image = "https://sabbath-school.adventech.io/api/v1/images/features/feature_inside_story.png"
                        )
                    )
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("title-1").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("title-2").assertIsDisplayed()
    }
}
