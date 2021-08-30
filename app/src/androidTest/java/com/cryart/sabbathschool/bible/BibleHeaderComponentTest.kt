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

package com.cryart.sabbathschool.bible

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.cryart.design.theme.SSTheme
import com.cryart.sabbathschool.bible.components.HeaderRow
import com.cryart.sabbathschool.test.di.mock.MockDataFactory
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class BibleHeaderComponentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val bibleVerses = MockDataFactory.bibleVerses

    @Test
    fun verify_last_bible_used_is_displayed() {
        composeTestRule.setContent {
            SSTheme {
                HeaderRow(
                    bibleVerses = bibleVerses,
                    lastBibleUsed = "KJV",
                )
            }
        }

        composeTestRule
            .onNodeWithText("KJV")
            .assertIsDisplayed()
    }

    @Test
    fun verify_bible_versions_are_displayed() {
        composeTestRule.setContent {
            SSTheme {
                HeaderRow(
                    bibleVerses = bibleVerses,
                    lastBibleUsed = "KJV",
                )
            }
        }

        bibleVerses.forEach { verses ->
            composeTestRule
                .onNodeWithTag("DropdownMenuItem-${verses.name}")
                .assertDoesNotExist()
        }

        composeTestRule
            .onNodeWithTag("drop-down")
            .performClick()

        bibleVerses.forEach { verses ->
            composeTestRule
                .onNodeWithTag("DropdownMenuItem-${verses.name}")
                .assertIsDisplayed()
        }
    }
}
