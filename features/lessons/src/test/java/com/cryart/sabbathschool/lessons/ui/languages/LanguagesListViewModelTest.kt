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

import app.cash.turbine.test
import app.ss.lessons.data.repository.quarterly.QuarterliesRepository
import app.ss.models.Language
import com.cryart.sabbathschool.core.extensions.prefs.SSPrefs
import com.cryart.sabbathschool.core.response.Resource
import com.cryart.sabbathschool.test.coroutines.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LanguagesListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val mockRepository: QuarterliesRepository = mockk()
    private val mockSSPrefs: SSPrefs = mockk()

    private val languagesFlow = MutableSharedFlow<Resource<List<Language>>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private lateinit var viewModel: LanguagesListViewModel

    @Before
    fun setup() {
        every { mockSSPrefs.getLanguageCode() }.returns("en")
        every { mockRepository.getLanguages() }.returns(languagesFlow)

        viewModel = LanguagesListViewModel(
            repository = mockRepository,
            ssPrefs = mockSSPrefs
        )
    }

    @Test
    fun `should emit models with properly formatted native language name`() = runTest {
        viewModel.languagesFlow.test {
            awaitItem() shouldBeEqualTo emptyList()

            languagesFlow.emit(
                Resource.success(
                    listOf(
                        Language("en", "English"),
                        Language("es", "Spanish"),
                        Language("fr", "French")
                    )
                )
            )

            awaitItem() shouldBeEqualTo listOf(
                LanguageModel("en", "English", "English", true),
                LanguageModel("es", "Español", "Spanish", false),
                LanguageModel("fr", "Français", "French", false)
            )
        }
    }

    @Test
    fun `should format native language name correctly`() {
        viewModel.getNativeLanguageName(Language("en", "English")) shouldBeEqualTo "English"
        viewModel.getNativeLanguageName(Language("es", "Spanish")) shouldBeEqualTo "Español"
        viewModel.getNativeLanguageName(Language("fr", "French")) shouldBeEqualTo "Français"
    }
}
