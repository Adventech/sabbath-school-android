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

package com.cryart.sabbathschool.lessons.ui.languages

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.ss.models.Language
import app.ss.lessons.data.repository.quarterly.QuarterliesRepository
import com.cryart.sabbathschool.core.extensions.prefs.SSPrefs
import com.cryart.sabbathschool.core.response.Resource
import com.cryart.sabbathschool.test.coroutines.TestDispatcherProvider
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LanguagesListViewModelTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private val dispatcherProvider = TestDispatcherProvider()

    private val mockRepository: QuarterliesRepository = mockk()
    private val mockSSPrefs: SSPrefs = mockk()

    private lateinit var viewModel: LanguagesListViewModel

    @Before
    fun setup() {
        viewModel = LanguagesListViewModel(
            mockRepository,
            mockSSPrefs,
            dispatcherProvider
        )
    }

    @Test
    fun `should emit models with properly formatted native language name`() = runTest {
        val languageList = listOf(
            Language("en", "English"),
            Language("es", "Spanish"),
            Language("fr", "French")
        )
        every { mockSSPrefs.getLanguageCode() }.returns("en")
        coEvery { mockRepository.getLanguages() }.returns(Resource.success(languageList))

        viewModel.languagesLiveData.observeForever {
            it shouldBeEqualTo listOf(
                LanguageModel("en", "English", "English", true),
                LanguageModel("es", "Español", "Spanish", false),
                LanguageModel("fr", "Français", "French", false)
            )
        }
    }
}
