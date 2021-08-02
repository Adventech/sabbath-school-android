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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import app.ss.lessons.data.model.SSQuarterly
import app.ss.lessons.data.repository.quarterly.QuarterliesRepository
import com.cryart.sabbathschool.core.response.Resource
import com.cryart.sabbathschool.core.extensions.arch.observeFuture
import com.cryart.sabbathschool.core.extensions.prefs.SSPrefs
import com.cryart.sabbathschool.core.misc.SSConstants
import com.cryart.sabbathschool.core.model.ViewState
import com.cryart.sabbathschool.test.coroutines.CoroutineTestRule
import com.cryart.sabbathschool.test.coroutines.runBlockingTest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldBeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.time.ExperimentalTime

@ExperimentalTime
class QuarterliesViewModelTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    @get:Rule
    var coroutinesTestRule = CoroutineTestRule()

    private val mockRepository: QuarterliesRepository = mockk(relaxed = true)
    private val mockSSPrefs: SSPrefs = mockk()
    private val mockSavedStateHandle: SavedStateHandle = mockk()

    private lateinit var viewModel: QuarterliesViewModel

    @Before
    fun setup() {
        every { mockSSPrefs.getLanguageCode() }.returns("en")
        every { mockSavedStateHandle.get<Boolean>(SSConstants.SS_QUARTERLY_SCREEN_LAUNCH_EXTRA) }.returns(true)

        viewModel = QuarterliesViewModel(
            mockRepository,
            mockSSPrefs,
            coroutinesTestRule.dispatcherProvider,
            mockSavedStateHandle
        )
    }

    @Test
    fun `should post last quarterly index if it exists on viewCreated`() {
        // given
        val quarterlyIndex = "en-2020-02-13"
        every { mockSSPrefs.getLastQuarterlyIndex() }.returns(quarterlyIndex)
        every { mockSSPrefs.isAppReBrandingPromptShown() }.returns(true)

        // when
        viewModel.viewCreated()

        // then
        viewModel.lastQuarterlyIndexLiveData.value shouldBeEqualTo quarterlyIndex
    }

    @Test
    fun `should not post any last quarterly index if it doesn't exists on viewCreated`() {
        // given
        every { mockSSPrefs.getLastQuarterlyIndex() }.returns(null)

        // when
        viewModel.viewCreated()

        // then
        viewModel.lastQuarterlyIndexLiveData.value.shouldBeNull()
    }

    @Test
    fun `should not post last quarterly index if it exists on viewCreated and branding prompt not seen`() {
        // given
        val quarterlyIndex = "en-2020-02-13"
        every { mockSSPrefs.getLastQuarterlyIndex() }.returns(quarterlyIndex)
        every { mockSSPrefs.isAppReBrandingPromptShown() }.returns(false)

        // when
        viewModel.viewCreated()

        // then
        viewModel.lastQuarterlyIndexLiveData.value.shouldBeNull()
    }

    @Test
    fun `should not post any last quarterly index if launch mode is not default`() {
        // given
        every { mockSavedStateHandle.get<Boolean>(SSConstants.SS_QUARTERLY_SCREEN_LAUNCH_EXTRA) }.returns(false)

        // when
        viewModel.viewCreated()

        // then
        viewModel.lastQuarterlyIndexLiveData.value.shouldBeNull()
    }

    @Test
    fun `should only post last quarterly index once if it exists on viewCreated`() {
        // given
        val quarterlyIndex = "en-2020-02-13"
        every { mockSSPrefs.getLastQuarterlyIndex() }.returns(quarterlyIndex)
        every { mockSSPrefs.isAppReBrandingPromptShown() }.returns(true)
        val indices = viewModel.lastQuarterlyIndexLiveData.observeFuture()

        // when
        with(viewModel) {
            viewCreated()
            viewCreated()
            viewCreated()
        }

        // then
        indices.size shouldBe 1
    }

    @Test
    fun `should update selected language and quarterlies list`() = runBlockingTest {
        // given
        val states = viewModel.viewStateLiveData.observeFuture()
        val language = "de"
        val flow: Flow<Resource<List<SSQuarterly>>> = callbackFlow {
            trySend(Resource.success(emptyList()))
            awaitClose { }
        }
        every { mockRepository.getQuarterlies(language) }.returns(flow)
        every { mockSSPrefs.setLanguageCode(language) }.returns(Unit)
        every { mockSSPrefs.isLanguagePromptSeen() }.returns(true)
        every { mockSSPrefs.isAppReBrandingPromptShown() }.returns(true)

        // when
        viewModel.languageSelected(language)

        // then
        verify { mockSSPrefs.setLanguageCode(language) }
        states shouldBeEqualTo listOf(
            ViewState.Loading,
            ViewState.Success(emptyList<SSQuarterly>())
        )
    }

    @Test
    fun `should emit true for branding prompt flow when not yet seen`() = coroutinesTestRule.runBlockingTest {
        val language = "de"
        val flow: Flow<Resource<List<SSQuarterly>>> = callbackFlow {
            trySend(Resource.success(emptyList()))
            awaitClose { }
        }
        every { mockRepository.getQuarterlies(language) }.returns(flow)
        every { mockSSPrefs.getLanguageCode() }.returns(language)
        every { mockSSPrefs.setLanguageCode(language) }.returns(Unit)
        every { mockSSPrefs.getLastQuarterlyIndex() }.returns(null)
        every { mockSSPrefs.isLanguagePromptSeen() }.returns(true)
        every { mockSSPrefs.isAppReBrandingPromptShown() }.returns(false)
        every { mockSSPrefs.setAppReBrandingShown() }.returns(Unit)

        viewModel.appReBrandingFlow.test {
            viewModel.viewCreated()

            expectItem().shouldBeTrue()

            verify { mockSSPrefs.setAppReBrandingShown() }
        }
    }

    @Test
    fun `should set languages prompt as seen`() {
        every { mockSSPrefs.setLanguagePromptSeen() }.returns(Unit)

        viewModel.languagesPromptSeen()

        verify { mockSSPrefs.setLanguagePromptSeen() }
    }

    @Test
    fun `should set view status to error when get quarterlies returns error resource`() =
        runBlockingTest {
            // given
            val states = viewModel.viewStateLiveData.observeFuture()
            val language = "de"
            val errorFlow: Flow<Resource<List<SSQuarterly>>> = callbackFlow {
                trySend(Resource.error(Throwable()))
                awaitClose { }
            }
            every { mockRepository.getQuarterlies(language) }.returns(errorFlow)
            every { mockSSPrefs.setLanguageCode(language) }.returns(Unit)
            every { mockSSPrefs.isLanguagePromptSeen() }.returns(true)

            // when
            viewModel.languageSelected(language)

            // then
            verify { mockSSPrefs.setLanguageCode(language) }
            states shouldBeEqualTo listOf(
                ViewState.Loading,
                ViewState.Error()
            )
        }
}
