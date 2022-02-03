package com.cryart.sabbathschool.readings

import app.cash.turbine.test
import app.ss.lessons.data.model.SSDay
import app.ss.models.SSLesson
import app.ss.lessons.data.model.SSLessonInfo
import app.ss.lessons.data.repository.lessons.LessonsRepository
import com.cryart.sabbathschool.core.misc.SSConstants
import com.cryart.sabbathschool.core.model.Status
import com.cryart.sabbathschool.core.response.Resource
import com.cryart.sabbathschool.readings.components.model.AppBarData
import com.cryart.sabbathschool.readings.components.model.ErrorData
import com.cryart.sabbathschool.readings.components.model.ReadingDaysData
import com.cryart.sabbathschool.test.coroutines.TestDispatcherProvider
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.junit.Before
import org.junit.Test
import kotlin.time.ExperimentalTime

@ExperimentalTime
class ReadingViewModelTest {

    private val dispatcherProvider = TestDispatcherProvider()

    private val mockLessonsRepository: LessonsRepository = mockk()

    private lateinit var viewModel: ReadingViewModel

    @Before
    fun setup() {
        viewModel = ReadingViewModel(mockLessonsRepository, dispatcherProvider)
    }

    @Test
    fun `uiStateFlow should emit Loading then Error when getLessonInfo call fails`() = runTest {
        val lessonIndex = "lesson"

        coEvery { mockLessonsRepository.getLessonInfo(lessonIndex) }
            .returns(Resource.error(Throwable()))

        viewModel.uiStateFlow.test {
            viewModel.loadData(lessonIndex)

            awaitItem() shouldBeEqualTo Status.LOADING
            awaitItem() shouldBeEqualTo Status.ERROR
        }
    }

    @Test
    fun `uiStateFlow should emit Loading then Error when read days are empty`() = runTest {
        val lessonIndex = "lesson"
        val lessonInfo = SSLessonInfo(SSLesson("Lesson"), emptyList())

        coEvery { mockLessonsRepository.getLessonInfo(lessonIndex) }
            .returns(Resource.success(lessonInfo))

        viewModel.uiStateFlow.test {
            viewModel.loadData(lessonIndex)

            awaitItem() shouldBeEqualTo Status.LOADING
            awaitItem() shouldBeEqualTo Status.ERROR
        }
    }

    @Test
    fun `errorData should emit Empty then errorRes when read days are empty`() = runTest {
        val lessonIndex = "lesson"
        val lessonInfo = SSLessonInfo(SSLesson("Lesson"), emptyList())

        coEvery { mockLessonsRepository.getLessonInfo(lessonIndex) }
            .returns(Resource.success(lessonInfo))

        viewModel.errorDataFlow.test {
            viewModel.loadData(lessonIndex)

            awaitItem() shouldBeEqualTo ErrorData.Empty
            awaitItem() shouldBeEqualTo ErrorData.Data(errorRes = R.string.ss_reading_empty)
        }
    }

    @Test
    fun `uiStateFlow should emit Loading then Success when read days are not empty`() = runTest {
        val lessonIndex = "lesson"
        val lessonInfo = SSLessonInfo(
            SSLesson("Lesson"),
            listOf(SSDay(title = "Day Title", date = "03/04/2021"))
        )

        coEvery { mockLessonsRepository.getLessonInfo(lessonIndex) }
            .returns(Resource.success(lessonInfo))

        viewModel.uiStateFlow.test {
            viewModel.loadData(lessonIndex)

            awaitItem() shouldBeEqualTo Status.LOADING
            awaitItem() shouldBeEqualTo Status.SUCCESS
        }
    }

    @Test
    fun `should default to empty string when parsing an invalid date (empty string)`() = runTest {
        val lessonIndex = "lesson"
        val lessonInfo = SSLessonInfo(
            SSLesson("Lesson"),
            listOf(SSDay(title = "Day Title", date = ""))
        )

        coEvery { mockLessonsRepository.getLessonInfo(lessonIndex) }
            .returns(Resource.success(lessonInfo))

        viewModel.uiStateFlow.test {
            viewModel.loadData(lessonIndex)

            awaitItem() shouldBeEqualTo Status.LOADING
            awaitItem() shouldBeEqualTo Status.SUCCESS
        }
    }

    @Test
    fun `should default to empty string when parsing an invalid date (invalid day of month)`() = runTest {
        val lessonIndex = "lesson"
        val lessonInfo = SSLessonInfo(
            SSLesson("Lesson"),
            listOf(SSDay(title = "Day Title", date = "32/03/2021"))
        )

        coEvery { mockLessonsRepository.getLessonInfo(lessonIndex) }
            .returns(Resource.success(lessonInfo))

        viewModel.uiStateFlow.test {
            viewModel.loadData(lessonIndex)

            awaitItem() shouldBeEqualTo Status.LOADING
            awaitItem() shouldBeEqualTo Status.SUCCESS
        }
    }

    @Test
    fun `appBarData should emit Empty then Cover`() = runTest {
        val lessonIndex = "lesson"
        val lessonInfo = SSLessonInfo(
            SSLesson("Lesson", cover = "cover_url"),
            listOf(SSDay(title = "Day Title", date = "03/04/2021"))
        )

        coEvery { mockLessonsRepository.getLessonInfo(lessonIndex) }
            .returns(Resource.success(lessonInfo))

        viewModel.appBarDataFlow.test {
            viewModel.loadData(lessonIndex)

            awaitItem() shouldBeEqualTo AppBarData.Empty
            awaitItem() shouldBeEqualTo AppBarData.Cover("cover_url")
        }
    }

    @Test
    fun `appBarData should emit Empty then Cover, then Title onPageSelected`() = runTest {
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

            awaitItem() shouldBeEqualTo AppBarData.Empty
            awaitItem() shouldBeEqualTo AppBarData.Cover("cover_url")
            awaitItem() shouldBeEqualTo AppBarData.Title("Day Title", "Saturday. 3 April")
        }
    }

    @Test
    fun `should set index as current day`() = runTest {
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

            awaitItem() shouldBeEqualTo ReadingDaysData.Empty
            (awaitItem() as ReadingDaysData.Days).index shouldBeEqualTo 1
        }
    }

    @Test
    fun `should update index on saveSelectedPage`() = runTest {
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

            awaitItem() shouldBeEqualTo ReadingDaysData.Empty
            (awaitItem() as ReadingDaysData.Days).index shouldBeEqualTo 1
            (awaitItem() as ReadingDaysData.Days).index shouldBeEqualTo 2
        }
    }
}
