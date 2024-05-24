package app.ss.quarterlies.list

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.ss.models.QuarterlyGroup
import app.ss.models.SSQuarterly
import app.ss.quarterlies.QuarterliesUseCaseImpl
import app.ss.quarterlies.model.GroupedQuarterlies
import app.ss.quarterlies.model.placeHolderQuarterlies
import app.ss.quarterlies.model.spec
import com.cryart.sabbathschool.core.navigation.Destination
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.test
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import org.junit.runner.RunWith
import ss.lessons.test.FakeQuarterliesRepository
import ss.libraries.circuit.navigation.LegacyDestination
import ss.misc.SSConstants
import ss.prefs.api.test.FakeSSPrefs

@RunWith(AndroidJUnit4::class)
class QuarterliesListPresenterTest {

    private val quarterlyGroup = QuarterlyGroup("Psalms", 1)
    private val fakeNavigator = FakeNavigator(QuarterliesListScreen(quarterlyGroup))
    private val fakeRepository = FakeQuarterliesRepository()
    private val fakePrefs = FakeSSPrefs(languagesFlow = MutableStateFlow("en"))

    private val underTest = QuarterliesListPresenter(
        navigator = fakeNavigator,
        screen = QuarterliesListScreen(quarterlyGroup),
        repository = fakeRepository,
        ssPrefs = fakePrefs,
        quarterliesUseCase = QuarterliesUseCaseImpl(),
    )

    @Test
    fun `present - emit state`() = runTest {
        val quarterlies = quarterliesList()
        fakeRepository.quarterliesMap["en" to quarterlyGroup] = flowOf(Result.success(quarterlies))

        underTest.test {
            var state = awaitItem()

            state.title shouldBeEqualTo quarterlyGroup.name
            state.type shouldBeEqualTo GroupedQuarterlies.TypeList(placeHolderQuarterlies())

            state = awaitItem()
            state.type shouldBeEqualTo GroupedQuarterlies.TypeList(quarterlies.map { it.spec() }.toImmutableList())

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - event - OnNavBack`() = runTest {
        val quarterlies = quarterliesList()
        fakeRepository.quarterliesMap["en" to quarterlyGroup] = flowOf(Result.success(quarterlies))

        underTest.test {
            awaitItem()

            val state = awaitItem()
            state.eventSink(QuarterliesListScreen.Event.OnNavBack)

            fakeNavigator.awaitPop()

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - event - QuarterlySelected`() = runTest {
        val quarterlies = quarterliesList()
        fakeRepository.quarterliesMap["en" to quarterlyGroup] = flowOf(Result.success(quarterlies))

        underTest.test {
            awaitItem()

            val state = awaitItem()
            state.eventSink(QuarterliesListScreen.Event.QuarterlySelected("index"))

            val screen = fakeNavigator.awaitNextScreen() as LegacyDestination
            with(screen) {
                destination shouldBeEqualTo Destination.LESSONS
                extras?.getString(SSConstants.SS_QUARTERLY_INDEX_EXTRA) shouldBeEqualTo "index"
            }

            ensureAllEventsConsumed()
        }
    }

    private fun quarterliesList(language: String = "en") = listOf(
        SSQuarterly(
            id = language,
            quarterly_group = QuarterlyGroup("Name", 1),
            color_primary = "#2E5797"
        )
    )
}
