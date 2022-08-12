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

package app.ss.pdf.ui

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.ss.models.LessonPdf
import app.ss.pdf.PdfReader
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SSReadPdfActivityTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var pdfReader: PdfReader

    private var scenario: ActivityScenario<SSReadPdfActivity>? = null

    @Before
    fun setup() {
        hiltRule.inject()

        Intents.init()
    }

    @After
    fun cleanUp() {
        Intents.release()
        scenario?.close()
    }

    @Test
    @Ignore("flaky")
    fun launchPdfScreen() = launch(
        listOf(
            LessonPdf(
                id = "7d328f229ae2532b48433f5a2fd72f2e9261c066084c44bbf967afa6f7bb1b87",
                title = "A Family Tree of God's Love",
                src = "https://sabbath-school-pdf.adventech.io/pdf/en/2021-03-pp/7d328f229ae2532b48433f5a2fd72f2e9261c066084c44bbf967afa6f7bb1b87/" +
                    "7d328f229ae2532b48433f5a2fd72f2e9261c066084c44bbf967afa6f7bb1b87.pdf"
            )
        )
    ) {
        // onView(withText("A Family Tree of God's Love")).check(matches(isDisplayed()))
    }

    private fun launch(
        pdfs: List<LessonPdf>,
        block: (ActivityScenario<SSReadPdfActivity>) -> Unit
    ) {
        val intent = pdfReader.launchIntent(pdfs, "")

        scenario = ActivityScenario.launch<SSReadPdfActivity>(intent).also {
            block(it)
        }
    }
}
