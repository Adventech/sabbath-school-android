package app.ss.quarterlies

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.ss.auth.test.FakeAuthRepository
import app.ss.models.QuarterlyGroup
import app.ss.models.SSQuarterly
import app.ss.models.auth.SSUser
import app.ss.quarterlies.list.QuarterliesListScreen
import app.ss.quarterlies.model.GroupedQuarterlies
import app.ss.quarterlies.model.placeHolderQuarterlies
import app.ss.quarterlies.model.spec
import app.ss.quarterlies.overlay.UserInfo
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
import ss.libraries.circuit.navigation.LanguagesScreen
import ss.libraries.circuit.navigation.LegacyDestination
import ss.libraries.circuit.navigation.LoginScreen
import ss.libraries.circuit.navigation.QuarterliesScreen
import ss.libraries.circuit.navigation.SettingsScreen
import ss.misc.SSConstants
import ss.prefs.api.test.FakeSSPrefs
import app.ss.quarterlies.overlay.AccountDialogOverlay.Result as OverlayResult

@RunWith(AndroidJUnit4::class)
class QuarterliesPresenterTest {

    private val selectedLanguageFlow = MutableStateFlow("en")

    private val fakeNavigator = FakeNavigator(QuarterliesScreen)
    private val fakeRepository = FakeQuarterliesRepository()
    private val fakeAuthRepository = FakeAuthRepository()
    private val fakePrefs = FakeSSPrefs(
        languagesFlow = selectedLanguageFlow
    )

    private val underTest = QuarterliesPresenter(
        navigator = fakeNavigator,
        repository = fakeRepository,
        authRepository = fakeAuthRepository,
        ssPrefs = fakePrefs,
        quarterliesUseCase = QuarterliesUseCaseImpl(),
    )

    @Test
    fun `present - emit state`() = runTest {
        val quarterlies = quarterliesList()
        fakeAuthRepository.userDelegate = { Result.success(SSUser.fake()) }
        fakeRepository.quarterliesMap["en" to null] = flowOf(Result.success(quarterlies))

        underTest.test {
            var state = awaitItem()

            state.photoUrl shouldBeEqualTo null
            state.type shouldBeEqualTo GroupedQuarterlies.TypeList(placeHolderQuarterlies())

            state = awaitItem()
            state.photoUrl shouldBeEqualTo ""
            state.type shouldBeEqualTo GroupedQuarterlies.TypeList(quarterlies.map { it.spec() }.toImmutableList())

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - event - QuarterlySelected`() = runTest {
        val quarterlies = quarterliesList()
        fakeAuthRepository.userDelegate = { Result.success(SSUser.fake()) }
        fakeRepository.quarterliesMap["en" to null] = flowOf(Result.success(quarterlies))

        underTest.test {
            var state = awaitItem()

            state.photoUrl shouldBeEqualTo null
            state.type shouldBeEqualTo GroupedQuarterlies.TypeList(placeHolderQuarterlies())

            state = awaitItem()
            state.eventSink(Event.QuarterlySelected("key"))

            val screen = fakeNavigator.awaitNextScreen() as LegacyDestination
            with(screen) {
                destination shouldBeEqualTo Destination.LESSONS
                extras?.getString(SSConstants.SS_QUARTERLY_INDEX_EXTRA) shouldBeEqualTo "key"
            }

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - event - FilterLanguages`() = runTest {
        val quarterlies = quarterliesList()
        fakeAuthRepository.userDelegate = { Result.success(SSUser.fake()) }
        fakeRepository.quarterliesMap["en" to null] = flowOf(Result.success(quarterlies))

        underTest.test {
            var state = awaitItem()

            state.photoUrl shouldBeEqualTo null
            state.type shouldBeEqualTo GroupedQuarterlies.TypeList(placeHolderQuarterlies())

            state = awaitItem()
            state.eventSink(Event.FilterLanguages)

            fakeNavigator.awaitNextScreen() shouldBeEqualTo LanguagesScreen

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - event - SeeAll`() = runTest {
        val quarterlies = quarterliesList()
        fakeAuthRepository.userDelegate = { Result.success(SSUser.fake()) }
        fakeRepository.quarterliesMap["en" to null] = flowOf(Result.success(quarterlies))

        underTest.test {
            var state = awaitItem()

            state.photoUrl shouldBeEqualTo null
            state.type shouldBeEqualTo GroupedQuarterlies.TypeList(placeHolderQuarterlies())

            state = awaitItem()
            state.eventSink(Event.SeeAll(QuarterlyGroup("Name", 1)))

            val screen = fakeNavigator.awaitNextScreen()
            screen shouldBeEqualTo QuarterliesListScreen(QuarterlyGroup("Name", 1))

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - event - ProfileClick`() = runTest {
        val quarterlies = quarterliesList()
        val user = SSUser.fake().copy(
            displayName = "Name",
            email = "Email",
            photo = "Photo"
        )
        fakeAuthRepository.userDelegate = { Result.success(user) }
        fakeRepository.quarterliesMap["en" to null] = flowOf(Result.success(quarterlies))

        underTest.test {
            var state = awaitItem()

            state.photoUrl shouldBeEqualTo null
            state.type shouldBeEqualTo GroupedQuarterlies.TypeList(placeHolderQuarterlies())

            state = awaitItem()
            state.photoUrl shouldBeEqualTo user.photo
            state.overlayState shouldBeEqualTo null

            state.eventSink(Event.ProfileClick)
            state = awaitItem()

            state.overlayState!!.userInfo shouldBeEqualTo UserInfo(user.displayName, user.email, user.photo)

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - overlay result - GoToAbout`() = runTest {
        val quarterlies = quarterliesList()
        fakeAuthRepository.userDelegate = { Result.success(SSUser.fake()) }
        fakeRepository.quarterliesMap["en" to null] = flowOf(Result.success(quarterlies))

        underTest.test {
            awaitItem()

            var state = awaitItem()

            state.eventSink(Event.ProfileClick)
            state = awaitItem()

            state.overlayState!!.onResult(OverlayResult.GoToAbout)

            state = awaitItem()
            state.overlayState shouldBeEqualTo null

            fakeNavigator.awaitNextScreen() shouldBeEqualTo LegacyDestination(Destination.ABOUT)

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - overlay result - Dismiss`() = runTest {
        val quarterlies = quarterliesList()
        fakeAuthRepository.userDelegate = { Result.success(SSUser.fake()) }
        fakeRepository.quarterliesMap["en" to null] = flowOf(Result.success(quarterlies))

        underTest.test {
            awaitItem()

            var state = awaitItem()
            state.eventSink(Event.ProfileClick)
            state = awaitItem()

            state.overlayState!!.onResult(OverlayResult.Dismiss)

            state = awaitItem()
            state.overlayState shouldBeEqualTo null

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - overlay result - GoToSettings`() = runTest {
        val quarterlies = quarterliesList()
        fakeAuthRepository.userDelegate = { Result.success(SSUser.fake()) }
        fakeRepository.quarterliesMap["en" to null] = flowOf(Result.success(quarterlies))

        underTest.test {
            awaitItem()

            var state = awaitItem()
            state.eventSink(Event.ProfileClick)
            state = awaitItem()

            state.overlayState!!.onResult(OverlayResult.GoToSettings)

            state = awaitItem()
            state.overlayState shouldBeEqualTo null

            fakeNavigator.awaitNextScreen() shouldBeEqualTo SettingsScreen

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - overlay result - SignOut`() = runTest {
        val quarterlies = quarterliesList()
        fakeAuthRepository.userDelegate = { Result.success(SSUser.fake()) }
        fakeRepository.quarterliesMap["en" to null] = flowOf(Result.success(quarterlies))

        underTest.test {
            awaitItem()

            var state = awaitItem()
            state.eventSink(Event.ProfileClick)
            state = awaitItem()

            state.overlayState!!.onResult(OverlayResult.SignOut)

            state = awaitItem()
            state.overlayState shouldBeEqualTo null

            fakeAuthRepository.logoutConfirmed shouldBeEqualTo true
            fakeNavigator.awaitResetRoot().newRoot shouldBeEqualTo LoginScreen

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
