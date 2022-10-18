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

package app.ss.languages

import app.cash.turbine.test
import app.ss.languages.state.LanguageModel
import app.ss.languages.state.LanguagesState
import app.ss.lessons.data.repository.quarterly.QuarterliesRepository
import app.ss.models.Language
import com.cryart.sabbathschool.core.extensions.prefs.SSPrefs
import com.cryart.sabbathschool.core.response.Resource
import com.cryart.sabbathschool.test.coroutines.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LanguagesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val mockRepository: QuarterliesRepository = mockk()
    private val mockSSPrefs: SSPrefs = mockk()

    private val languagesFlow = MutableSharedFlow<Resource<List<Language>>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private lateinit var viewModel: LanguagesViewModel

    @Before
    fun setup() {
        every { mockSSPrefs.getLanguageCode() }.returns("en")
        every { mockRepository.getLanguages("") }.returns(languagesFlow)

        viewModel = LanguagesViewModel(
            repository = mockRepository,
            ssPrefs = mockSSPrefs
        )
    }

    @Test
    fun `should emit models with properly formatted native language name`() = runTest {
        viewModel.uiState.test {
            awaitItem() shouldBeEqualTo LanguagesState()

            languagesFlow.emit(
                Resource.success(
                    listOf(
                        Language("en", "English"),
                        Language("es", "Spanish"),
                        Language("fr", "French")
                    )
                )
            )

            awaitItem().listState.models shouldBeEqualTo listOf(
                LanguageModel("en", "English", "English", true),
                LanguageModel("es", "Spanish", "Español", false),
                LanguageModel("fr", "French", "Français", false)
            )
        }
    }

    @Test
    fun `should perform query`() = runTest {
        every { mockRepository.getLanguages("query") }.returns(languagesFlow)

        val job = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        viewModel.uiState.test {
            awaitItem() shouldBeEqualTo LanguagesState()

            viewModel.searchFor("query")
            languagesFlow.emit(Resource.success(emptyList()))

            awaitItem() shouldBeEqualTo LanguagesState(
                isLoading = false,
                query = "query"
            )
        }

        job.cancel()
    }

    @Test
    fun `should trim query`() = runTest {
        every { mockRepository.getLanguages("query") }.returns(languagesFlow)

        val job = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        viewModel.uiState.test {
            awaitItem() shouldBeEqualTo LanguagesState()

            viewModel.searchFor("  query  ")
            languagesFlow.emit(Resource.success(emptyList()))

            awaitItem() shouldBeEqualTo LanguagesState(
                isLoading = false,
                query = "query"
            )
        }

        job.cancel()
    }


    @Test
    fun `should format native language name correctly`() {
        viewModel.getNativeLanguageName(Language("en", "English")) shouldBeEqualTo "English"
        viewModel.getNativeLanguageName(Language("es", "Spanish")) shouldBeEqualTo "Español"
        viewModel.getNativeLanguageName(Language("fr", "French")) shouldBeEqualTo "Français"
    }

    @Test
    fun `should save selected languages`() {
        every { mockSSPrefs.setLanguageCode("es") }.returns(Unit)
        every { mockSSPrefs.setLastQuarterlyIndex(null) }.returns(Unit)

        viewModel.modelSelected(LanguageModel("es", "Spanish", "Español", false))

        verify {
            mockSSPrefs.setLanguageCode("es")
            mockSSPrefs.setLastQuarterlyIndex(null)
        }
    }
}
