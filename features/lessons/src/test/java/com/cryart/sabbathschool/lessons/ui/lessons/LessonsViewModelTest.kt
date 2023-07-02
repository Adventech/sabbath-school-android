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

package com.cryart.sabbathschool.lessons.ui.lessons

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import app.ss.lessons.data.repository.lessons.LessonsRepository
import app.ss.lessons.data.repository.quarterly.QuarterliesRepository
import app.ss.models.LessonPdf
import app.ss.models.PublishingInfo
import app.ss.models.SSLesson
import app.ss.models.SSLessonInfo
import app.ss.models.SSQuarterlyInfo
import app.ss.widgets.AppWidgetHelper
import com.cryart.sabbathschool.core.response.Resource
import com.cryart.sabbathschool.lessons.navigation.lessonIndexArg
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
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
import ss.foundation.coroutines.test.MainDispatcherRule
import ss.prefs.api.SSPrefs

private const val QUARTERLY_INDEX = "quarterly_index"

class LessonsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val mockQuarterliesRepository: QuarterliesRepository = mockk()
    private val mockLessonsRepository: LessonsRepository = mockk()
    private val mockPrefs: SSPrefs = mockk()
    private val mockWidgetHelper: AppWidgetHelper = mockk()
    private val mockSavedStateHandle: SavedStateHandle = mockk()

    private val publishingInfoFlow = MutableSharedFlow<Resource<PublishingInfo>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val quarterlyInfoFlow = MutableSharedFlow<Resource<SSQuarterlyInfo>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private lateinit var viewModel: LessonsViewModel

    @Before
    fun setUp() {
        every { mockSavedStateHandle.get<String>(lessonIndexArg) }.returns(QUARTERLY_INDEX)
        every { mockWidgetHelper.refreshAll() } returns Unit
        every { mockPrefs.getDisplayOptions(any()) }.returns(Unit)
        every { mockQuarterliesRepository.getPublishingInfo() } returns publishingInfoFlow
        every { mockQuarterliesRepository.getQuarterlyInfo(QUARTERLY_INDEX) } returns quarterlyInfoFlow

        viewModel = LessonsViewModel(
            repository = mockQuarterliesRepository,
            lessonsRepository = mockLessonsRepository,
            ssPrefs = mockPrefs,
            appWidgetHelper = mockWidgetHelper,
            savedStateHandle = mockSavedStateHandle
        )
    }

    @Test
    fun `viewState initial state`() {
        viewModel.uiState.value shouldBeEqualTo LessonsScreenState()
    }

    @Test
    fun `viewState success when both quarterlyInfo and publishing Info load`() = runTest {
        val mockPublishingInfo = mockk<PublishingInfo>()
        val mockQuarterlyInfo = mockk<SSQuarterlyInfo>()

        val job = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        viewModel.uiState.test {
            awaitItem() shouldBeEqualTo LessonsScreenState()

            quarterlyInfoFlow.emit(Resource.success(mockQuarterlyInfo))

            awaitItem() shouldBeEqualTo LessonsScreenState(
                isLoading = false,
                isError = false,
                publishingInfo = PublishingInfoState.Loading,
                quarterlyInfo = QuarterlyInfoState.Success(mockQuarterlyInfo)
            )

            publishingInfoFlow.emit(Resource.success(mockPublishingInfo))

            awaitItem() shouldBeEqualTo LessonsScreenState(
                isLoading = false,
                isError = false,
                publishingInfo = PublishingInfoState.Success(mockPublishingInfo),
                quarterlyInfo = QuarterlyInfoState.Success(mockQuarterlyInfo)
            )
        }

        job.cancel()
    }

    @Test
    fun `viewState error when QuarterlyInfo fails to load`() = runTest {
        val job = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        viewModel.uiState.test {
            awaitItem() shouldBeEqualTo LessonsScreenState()

            quarterlyInfoFlow.emit(Resource.error(Throwable()))

            awaitItem() shouldBeEqualTo LessonsScreenState(
                isLoading = false,
                isError = true,
                publishingInfo = PublishingInfoState.Loading,
                quarterlyInfo = QuarterlyInfoState.Error
            )
        }

        job.cancel()
    }

    @Test
    fun `viewState loading when PublishingInfo fails to load`() = runTest {
        val job = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        viewModel.uiState.test {
            awaitItem() shouldBeEqualTo LessonsScreenState()

            publishingInfoFlow.emit(Resource.error(Throwable()))

            awaitItem() shouldBeEqualTo LessonsScreenState(
                isLoading = true,
                isError = false,
                publishingInfo = PublishingInfoState.Error,
                quarterlyInfo = QuarterlyInfoState.Loading
            )
        }

        job.cancel()
    }

    @Test
    fun `should emit selected lesson pdf`() = runTest {
        val lessonIndex = "index"
        val lessonPdfs = listOf(LessonPdf("123"))
        val lesson = mockk<SSLesson>().apply {
            every { index } returns lessonIndex
        }
        val mockLessonInfo = mockk<SSLessonInfo>().apply {
            every { pdfs } returns lessonPdfs
        }
        coEvery { mockLessonsRepository.getLessonInfo(lessonIndex, false) }
            .returns(Resource.success(mockLessonInfo))

        viewModel.selectedPdfsFlow.test {
            viewModel.pdfLessonSelected(lesson.index)

            awaitItem() shouldBeEqualTo (lessonIndex to lessonPdfs)
        }
    }
}
