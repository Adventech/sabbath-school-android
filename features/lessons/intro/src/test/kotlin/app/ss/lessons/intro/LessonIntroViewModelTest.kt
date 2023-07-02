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

package app.ss.lessons.intro

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import app.ss.lessons.data.repository.quarterly.QuarterliesRepository
import app.ss.models.LessonIntroModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import ss.foundation.coroutines.test.MainDispatcherRule
import ss.misc.SSConstants

private const val INDEX = "en-2022-03"

class LessonIntroViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val mockRepository: QuarterliesRepository = mockk()
    private val mockSavedStateHandle: SavedStateHandle = mockk()

    private lateinit var viewModel: LessonIntroViewModel

    @Before
    fun setup() {
        every { mockSavedStateHandle.get<String>(SSConstants.SS_QUARTERLY_INDEX_EXTRA) }
            .returns(INDEX)
    }

    @Test
    fun `should emit Success when intro exists`() = runTest {
        coEvery { mockRepository.getIntro(INDEX) }.returns(
            Result.success(LessonIntroModel(INDEX, "Title", "Intro"))
        )
        viewModel = LessonIntroViewModel(
            repository = mockRepository,
            savedStateHandle = mockSavedStateHandle
        )

        viewModel.uiState.test {
            awaitItem() shouldBeEqualTo LessonIntroState.Success("Title", "Intro")
        }
    }

    @Test
    fun `should emit Error when intro doesn't exist`() = runTest {
        coEvery { mockRepository.getIntro(INDEX) }.returns(
            Result.failure(Throwable())
        )
        viewModel = LessonIntroViewModel(
            repository = mockRepository,
            savedStateHandle = mockSavedStateHandle
        )

        viewModel.uiState.test {
            awaitItem() shouldBeEqualTo LessonIntroState.Error
        }
    }
}
