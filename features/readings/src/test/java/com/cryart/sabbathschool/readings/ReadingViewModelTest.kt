package com.cryart.sabbathschool.readings

import app.cash.turbine.test
import app.ss.lessons.data.model.SSDay
import app.ss.lessons.data.model.SSLesson
import app.ss.lessons.data.model.SSLessonInfo
import app.ss.lessons.data.repository.lessons.LessonsRepository
import app.ss.lessons.data.response.Resource
import com.cryart.sabbathschool.core.misc.SSConstants
import com.cryart.sabbathschool.readings.components.model.AppBarData
import com.cryart.sabbathschool.readings.components.model.ErrorData
import com.cryart.sabbathschool.readings.components.model.ReadingDaysData
import com.cryart.sabbathschool.test.coroutines.CoroutineTestRule
import com.cryart.sabbathschool.test.coroutines.runBlockingTest
import io.mockk.coEvery
import io.mockk.mockk
import org.amshove.kluent.shouldBeEqualTo
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
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

    @Test
    fun `errorData should emit Empty then errorRes when read days are empty`() = coroutinesTestRule.runBlockingTest {
        val lessonIndex = "lesson"
        val lessonInfo = SSLessonInfo(SSLesson("Lesson"), emptyList())

        coEvery { mockLessonsRepository.getLessonInfo(lessonIndex) }
            .returns(Resource.success(lessonInfo))

        viewModel.errorDataFlow.test {
            viewModel.loadData(lessonIndex)

            expectItem() shouldBeEqualTo ErrorData.Empty
            expectItem() shouldBeEqualTo ErrorData.Data(errorRes = R.string.ss_reading_empty)
        }
    }

    @Test
    fun `uiStateFlow should emit Loading then Success when read days are not empty`() = coroutinesTestRule.runBlockingTest {
        val lessonIndex = "lesson"
        val lessonInfo = SSLessonInfo(
            SSLesson("Lesson"),
            listOf(SSDay(title = "Day Title", date = "03/04/2021"))
        )

        coEvery { mockLessonsRepository.getLessonInfo(lessonIndex) }
            .returns(Resource.success(lessonInfo))

        viewModel.uiStateFlow.test {
            viewModel.loadData(lessonIndex)

            expectItem() shouldBeEqualTo UiState.Loading
            expectItem() shouldBeEqualTo UiState.Success
        }
    }

    @Test
    fun `should default to empty string when parsing an invalid date (empty string)`() = coroutinesTestRule.runBlockingTest {
        val lessonIndex = "lesson"
        val lessonInfo = SSLessonInfo(
            SSLesson("Lesson"),
            listOf(SSDay(title = "Day Title", date = ""))
        )

        coEvery { mockLessonsRepository.getLessonInfo(lessonIndex) }
            .returns(Resource.success(lessonInfo))

        viewModel.uiStateFlow.test {
            viewModel.loadData(lessonIndex)

            expectItem() shouldBeEqualTo UiState.Loading
            expectItem() shouldBeEqualTo UiState.Success
        }
    }

    @Test
    fun `should default to empty string when parsing an invalid date (invalid day of month)`() = coroutinesTestRule.runBlockingTest {
        val lessonIndex = "lesson"
        val lessonInfo = SSLessonInfo(
            SSLesson("Lesson"),
            listOf(SSDay(title = "Day Title", date = "32/03/2021"))
        )

        coEvery { mockLessonsRepository.getLessonInfo(lessonIndex) }
            .returns(Resource.success(lessonInfo))

        viewModel.uiStateFlow.test {
            viewModel.loadData(lessonIndex)

            expectItem() shouldBeEqualTo UiState.Loading
            expectItem() shouldBeEqualTo UiState.Success
        }
    }

    @Test
    fun `appBarData should emit Empty then Cover`() = coroutinesTestRule.runBlockingTest {
        val lessonIndex = "lesson"
        val lessonInfo = SSLessonInfo(
            SSLesson("Lesson", cover = "cover_url"),
            listOf(SSDay(title = "Day Title", date = "03/04/2021"))
        )

        coEvery { mockLessonsRepository.getLessonInfo(lessonIndex) }
            .returns(Resource.success(lessonInfo))

        viewModel.appBarDataFlow.test {
            viewModel.loadData(lessonIndex)

            expectItem() shouldBeEqualTo AppBarData.Empty
            expectItem() shouldBeEqualTo AppBarData.Cover("cover_url")
        }
    }

    @Test
    fun `appBarData should emit Empty then Cover, then Title onPageSelected`() = coroutinesTestRule.runBlockingTest {
        val lessonIndex = "lesson"
        val lessonInfo = SSLessonInfo(
            SSLesson("Lesson", cover = "cover_url"),
            listOf(SSDay(title = "Day Title", date = "03/04/2021"))
        )

        coEvery { mockLessonsRepository.getLessonInfo(lessonIndex) }
            .returns(Resource.success(lessonInfo))

        viewModel.appBarDataFlow.test {
            viewModel.loadData(lessonIndex)
            viewModel.onPageSelected(0)

            expectItem() shouldBeEqualTo AppBarData.Empty
            expectItem() shouldBeEqualTo AppBarData.Cover("cover_url")
            expectItem() shouldBeEqualTo AppBarData.Title("Day Title", "Saturday. 3 April")
        }
    }

    @Test
    fun `should set index as current day`() = coroutinesTestRule.runBlockingTest {
        val dateFormat = DateTimeFormat.forPattern(SSConstants.SS_DATE_FORMAT)
        val today = DateTime.now().withTimeAtStartOfDay()
        val yesterday = today.minusDays(1)
        val tomorrow = today.plusDays(1)

        val ssDays = listOf(
            SSDay(title = "Day Yesterday", date = dateFormat.print(yesterday)),
            SSDay(title = "Day Today", date = dateFormat.print(today)),
            SSDay(title = "Day Tomorrow", date = dateFormat.print(tomorrow)),
        )

        val lessonIndex = "lesson"
        val lessonInfo = SSLessonInfo(SSLesson("Lesson", cover = "cover_url"), ssDays)

        coEvery { mockLessonsRepository.getLessonInfo(lessonIndex) }
            .returns(Resource.success(lessonInfo))

        viewModel.readDaysFlow.test {
            viewModel.loadData(lessonIndex)

            expectItem() shouldBeEqualTo ReadingDaysData.Empty
            (expectItem() as ReadingDaysData.Days).index shouldBeEqualTo 1
        }
    }

    @Test
    fun `should update index on saveSelectedPage`() = coroutinesTestRule.runBlockingTest {
        val dateFormat = DateTimeFormat.forPattern(SSConstants.SS_DATE_FORMAT)
        val today = DateTime.now().withTimeAtStartOfDay()
        val yesterday = today.minusDays(1)
        val tomorrow = today.plusDays(1)

        val ssDays = listOf(
            SSDay(title = "Day Yesterday", date = dateFormat.print(yesterday)),
            SSDay(title = "Day Today", date = dateFormat.print(today)),
            SSDay(title = "Day Tomorrow", date = dateFormat.print(tomorrow)),
        )

        val lessonIndex = "lesson"
        val lessonInfo = SSLessonInfo(SSLesson("Lesson", cover = "cover_url"), ssDays)

        coEvery { mockLessonsRepository.getLessonInfo(lessonIndex) }
            .returns(Resource.success(lessonInfo))

        viewModel.readDaysFlow.test {
            viewModel.loadData(lessonIndex)
            viewModel.saveSelectedPage(2)

            expectItem() shouldBeEqualTo ReadingDaysData.Empty
            (expectItem() as ReadingDaysData.Days).index shouldBeEqualTo 1
            (expectItem() as ReadingDaysData.Days).index shouldBeEqualTo 2
        }
    }
}
