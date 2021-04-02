package com.cryart.sabbathschool.readings

import app.cash.turbine.test
import app.ss.lessons.data.model.SSLesson
import app.ss.lessons.data.model.SSLessonInfo
import app.ss.lessons.data.repository.lessons.LessonsRepository
import app.ss.lessons.data.response.Resource
import com.cryart.sabbathschool.test.coroutines.CoroutineTestRule
import com.cryart.sabbathschool.test.coroutines.runBlockingTest
import io.mockk.coEvery
import io.mockk.mockk
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.time.ExperimentalTime

@ExperimentalTime
class ReadingViewModelTest {

    @get:Rule
    var coroutinesTestRule = CoroutineTestRule()

    private val mockLessonsRepository: LessonsRepository = mockk()

    private lateinit var viewModel: ReadingViewModel

    @Before
    fun setup() {
        viewModel = ReadingViewModel(mockLessonsRepository, coroutinesTestRule.dispatcherProvider)
    }

    @Test
    fun `uiStateFlow should emit Loading then Error when getLessonInfo call fails`() = coroutinesTestRule.runBlockingTest {
        val lessonIndex = "lesson"

        coEvery { mockLessonsRepository.getLessonInfo(lessonIndex) }
            .returns(Resource.error(Throwable()))

        viewModel.uiStateFlow.test {
            viewModel.loadData(lessonIndex)

            expectItem() shouldBeEqualTo UiState.Loading
            expectItem() shouldBeEqualTo UiState.Error
        }
    }

    @Test
    fun `uiStateFlow should emit Loading then Error when read days are empty`() = coroutinesTestRule.runBlockingTest {
        val lessonIndex = "lesson"
        val lessonInfo = SSLessonInfo(SSLesson("Lesson"), emptyList())

        coEvery { mockLessonsRepository.getLessonInfo(lessonIndex) }
            .returns(Resource.success(lessonInfo))

        viewModel.uiStateFlow.test {
            viewModel.loadData(lessonIndex)

            expectItem() shouldBeEqualTo UiState.Loading
            expectItem() shouldBeEqualTo UiState.Error
        }
    }
}
