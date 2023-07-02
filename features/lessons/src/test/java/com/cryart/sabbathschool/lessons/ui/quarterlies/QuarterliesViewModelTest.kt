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

package com.cryart.sabbathschool.lessons.ui.quarterlies

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import app.ss.auth.AuthRepository
import app.ss.lessons.data.repository.quarterly.QuarterliesRepository
import app.ss.models.QuarterlyGroup
import app.ss.models.SSQuarterly
import app.ss.models.auth.SSUser
import com.cryart.sabbathschool.core.response.Resource
import com.cryart.sabbathschool.lessons.ui.quarterlies.model.GroupedQuarterlies
import com.cryart.sabbathschool.lessons.ui.quarterlies.model.spec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import ss.foundation.coroutines.test.MainDispatcherRule
import ss.misc.SSConstants
import ss.prefs.api.SSPrefs

class QuarterliesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val mockRepository: QuarterliesRepository = mockk()
    private val mockSSPrefs: SSPrefs = mockk()
    private val mockSavedStateHandle: SavedStateHandle = mockk()
    private val mockAuthRepository: AuthRepository = mockk()

    private val selectedLanguageFlow = MutableSharedFlow<String>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private lateinit var viewModel: QuarterliesViewModel

    @Before
    fun setup() {
        with(mockSSPrefs) {
            every { getLanguageCodeFlow() }.returns(selectedLanguageFlow)
            every { setLanguageCode(any()) }.returns(Unit)
            every { isAppReBrandingPromptShown() }.returns(true)
        }
        every { mockSavedStateHandle.get<QuarterlyGroup>(SSConstants.SS_QUARTERLY_GROUP) }.returns(null)
        val user = mockk<SSUser>()
        every { user.photo }.returns(null)
        every { mockAuthRepository.getUserFlow() }.returns(flowOf(user))

        with(mockRepository) {
            every { getQuarterlies("en", null) }.returns(
                flowOf(Resource.success(quarterliesList("en")))
            )
            every { getQuarterlies("de", null) }.returns(
                flowOf(Resource.success(quarterliesList("de")))
            )
        }

        viewModel = QuarterliesViewModel(
            repository = mockRepository,
            ssPrefs = mockSSPrefs,
            authRepository = mockAuthRepository,
            savedStateHandle = mockSavedStateHandle
        )
    }

    @Test
    fun `viewState initial state`() {
        viewModel.uiState.value shouldBeEqualTo QuarterliesUiState()
    }

    @Test
    fun `should update selected language`() {
        every { mockSSPrefs.setLastQuarterlyIndex(null) }.returns(Unit)

        viewModel.languageSelected("de")

        verify {
            mockSSPrefs.setLanguageCode("de")
            mockSSPrefs.setLastQuarterlyIndex(null)
        }
    }

    @Test
    fun `should update quarterlies list when language has changed`() = runTest {
        val job = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        viewModel.uiState.test {
            awaitItem() shouldBeEqualTo QuarterliesUiState()

            selectedLanguageFlow.emit("en")

            awaitItem() shouldBeEqualTo QuarterliesUiState(
                isLoading = false,
                isError = false,
                type = GroupedQuarterlies.TypeList(quarterlySpecs("en"))
            )

            selectedLanguageFlow.emit("de")

            awaitItem() shouldBeEqualTo QuarterliesUiState(
                isLoading = false,
                isError = false,
                type = GroupedQuarterlies.TypeList(quarterlySpecs("de"))
            )
        }

        job.cancel()
    }

    private fun quarterliesList(language: String) = listOf(
        SSQuarterly(
            id = language,
            quarterly_group = QuarterlyGroup("Name", 1),
            color_primary = "#2E5797"
        )
    )

    private fun quarterlySpecs(language: String) =
        quarterliesList(language)
            .map { it.spec() }
}
