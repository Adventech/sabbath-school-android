package app.ss.lessons

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.ss.lessons.components.LessonItemSpec
import app.ss.models.OfflineState
import app.ss.models.PublishingInfo
import app.ss.models.SSLesson
import app.ss.models.SSQuarterly
import app.ss.models.SSQuarterlyInfo
import com.cryart.sabbathschool.core.navigation.Destination
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.test
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import org.junit.runner.RunWith
import ss.lessons.test.FakeQuarterliesRepository
import ss.libraries.appwidget.test.FakeAppWidgetHelper
import ss.libraries.circuit.navigation.CustomTabsIntentScreen
import ss.libraries.circuit.navigation.LegacyDestination
import ss.libraries.circuit.navigation.LessonsScreen
import ss.misc.SSConstants
import ss.prefs.api.test.FakeSSPrefs
import ss.workers.api.test.FakeWorkScheduler

@RunWith(AndroidJUnit4::class)
class LessonsPresenterTest {

    private val screen = LessonsScreen("en-2024-02")
    private val fakeNavigator = FakeNavigator(screen)
    private val fakeRepository = FakeQuarterliesRepository()
    private val fakePrefs = FakeSSPrefs()
    private val fakeAppWidgetHelper = FakeAppWidgetHelper()
    private val fakeWorkScheduler = FakeWorkScheduler()

    private val underTest = LessonsPresenter(
        navigator = fakeNavigator,
        screen = screen,
        repository = fakeRepository,
        ssPrefs = fakePrefs,
        appWidgetHelper = fakeAppWidgetHelper,
        workScheduler = fakeWorkScheduler,
    )

    @Test
    fun `present - emit state`() = runTest {
        val quarterly = SSQuarterlyInfo(
            quarterly = SSQuarterly(id = "id"),
            lessons = emptyList()
        )
        val publishingInfo = PublishingInfo("message", "url")
        val publishingInfoFlow = MutableSharedFlow<Result<PublishingInfo>>(
            replay = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
        fakeRepository.quarterlyInfoMap[screen.quarterlyIndex!!] = Result.success(quarterly)
        fakeRepository.publishingInfoFlow = publishingInfoFlow

        underTest.test {
            awaitItem() shouldBeEqualTo State.Loading

            var state = awaitItem() as State.Success
            state.quarterlyInfo shouldBeEqualTo quarterly
            fakeAppWidgetHelper.syncedIndex shouldBeEqualTo quarterly.quarterly.index

            publishingInfoFlow.emit(Result.success(publishingInfo))

            state = awaitItem() as State.Success
            state.publishingInfo shouldBeEqualTo publishingInfo

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - event - OnLessonClick`() = runTest {
        val lesson = SSLesson(title = "Lesson title", index = "lesson-index")
        val quarterly = SSQuarterlyInfo(
            quarterly = SSQuarterly(id = "id"),
            lessons = listOf(lesson)
        )
        fakeRepository.quarterlyInfoMap[screen.quarterlyIndex!!] = Result.success(quarterly)

        underTest.test {
            awaitItem() shouldBeEqualTo State.Loading

            val state = awaitItem() as State.Success
            state.eventSink(
                Event.OnLessonClick(
                    LessonItemSpec(
                        index = "lesson-index",
                        displayIndex = "1",
                        title = "Lesson title",
                        date = "2024-02-01",
                        pdfOnly = false,
                        path = "path"
                    )
                )
            )

            with(fakeNavigator.awaitNextScreen() as LegacyDestination) {
                destination shouldBeEqualTo Destination.READ
                extras?.getString(SSConstants.SS_LESSON_INDEX_EXTRA) shouldBeEqualTo "lesson-index"
            }

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - event - OnNavigateBackClick`() = runTest {
        val quarterly = SSQuarterlyInfo(
            quarterly = SSQuarterly(id = "id"),
            lessons = emptyList()
        )

        fakeRepository.quarterlyInfoMap[screen.quarterlyIndex!!] = Result.success(quarterly)

        underTest.test {
            awaitItem() shouldBeEqualTo State.Loading

            val state = awaitItem() as State.Success
            state.eventSink(Event.OnNavigateBackClick)

            fakeNavigator.awaitPop()

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - event - OnOfflineStateClick`() = runTest {
        val index = "en-2024-02"
        val quarterly = SSQuarterlyInfo(
            quarterly = SSQuarterly(id = "id", index = index),
            lessons = emptyList()
        )

        fakeRepository.quarterlyInfoMap[screen.quarterlyIndex!!] = Result.success(quarterly)

        underTest.test {
            awaitItem() shouldBeEqualTo State.Loading

            val state = awaitItem() as State.Success
            state.eventSink(Event.OnOfflineStateClick)

            fakeWorkScheduler.quarterlySyncIndex shouldBeEqualTo index

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - event - OnOfflineStateClick - IN_PROGRESS is no-op`() = runTest {
        val quarterly = SSQuarterlyInfo(
            quarterly = SSQuarterly(id = "id", index = "en-2024-02", offlineState = OfflineState.IN_PROGRESS),
            lessons = emptyList()
        )

        fakeRepository.quarterlyInfoMap[screen.quarterlyIndex!!] = Result.success(quarterly)

        underTest.test {
            awaitItem() shouldBeEqualTo State.Loading

            val state = awaitItem() as State.Success
            state.eventSink(Event.OnOfflineStateClick)

            fakeWorkScheduler.quarterlySyncIndex shouldBeEqualTo null

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - event - OnReadMoreClick`() = runTest {
        val introduction = "Quarterly Introduction"
        val quarterly = SSQuarterlyInfo(
            quarterly = SSQuarterly(id = "id", introduction = introduction),
            lessons = emptyList()
        )

        fakeRepository.quarterlyInfoMap[screen.quarterlyIndex!!] = Result.success(quarterly)

        underTest.test {
            awaitItem() shouldBeEqualTo State.Loading

            var state = awaitItem() as State.Success
            state.overlayState shouldBeEqualTo null
            state.eventSink(Event.OnReadMoreClick)

            state = awaitItem() as State.Success
            with(state.overlayState!!) {
                content shouldBeEqualTo introduction
                onResult(ReadMoreOverlayState.Result.Dismissed)
            }
            state = awaitItem() as State.Success
            state.overlayState shouldBeEqualTo null

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - event - OnPublishingInfoClick`() = runTest {
        val quarterly = SSQuarterlyInfo(
            quarterly = SSQuarterly(id = "id"),
            lessons = emptyList()
        )
        val publishingInfo = PublishingInfo("message", "url")

        fakeRepository.quarterlyInfoMap[screen.quarterlyIndex!!] = Result.success(quarterly)
        fakeRepository.publishingInfoFlow = flowOf(Result.success(publishingInfo))

        underTest.test {
            awaitItem() shouldBeEqualTo State.Loading

            val state = awaitItem() as State.Success
            state.publishingInfo shouldBeEqualTo publishingInfo

            state.eventSink(Event.OnPublishingInfoClick)
            val screen = fakeNavigator.awaitNextScreen() as CustomTabsIntentScreen
            screen.url shouldBeEqualTo publishingInfo.url

            ensureAllEventsConsumed()
        }
    }
}
