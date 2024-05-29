/*
 * Copyright (c) 2023. Adventech <info@adventech.io>
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
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package app.ss.tv.presentation.home

import app.ss.tv.presentation.FakeScrollEvents
import app.ss.tv.presentation.Screens
import app.ss.tv.presentation.account.AccountScreen
import app.ss.tv.presentation.home.HomeScreen.Event
import app.ss.tv.presentation.videos.VideosScreen
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import ss.foundation.coroutines.test.TestDispatcherProvider
import ss.prefs.api.test.FakeSSPrefs

/** Unit tests for [HomePresenter]. */
class HomePresenterTest {

    private val fakeNavigator = FakeNavigator(HomeScreen)
    private val isTopBarVisible = MutableStateFlow(true)
    private val languagesFlow = MutableStateFlow("en")

    private val underTest = HomePresenter(
        navigator = fakeNavigator,
        scrollEvents = FakeScrollEvents(isTopBarVisible),
        ssPrefs = FakeSSPrefs(languagesFlow),
        dispatcherProvider = TestDispatcherProvider()
    )

    @Test
    fun `present - default state`() = runTest {
        underTest.test {
            with(awaitItem()) {
                selectedIndex shouldBeEqualTo 0
                currentScreen shouldBeEqualTo VideosScreen
                topAppBarVisible shouldBeEqualTo true
            }

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `event - switch to Accounts screen`() = runTest {
        underTest.test {
            val state = awaitItem()
            state.eventSink(Event.OnTopBarScreen(Screens.Account))

            awaitItem().currentScreen shouldBeEqualTo AccountScreen

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - switch to VideosScreen when language changes`() = runTest {
        underTest.test {
            val state = awaitItem()
            state.eventSink(Event.OnTopBarScreen(Screens.Account))

            awaitItem().currentScreen shouldBeEqualTo AccountScreen

            languagesFlow.value = "fr"

            awaitItem().currentScreen shouldBeEqualTo VideosScreen

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `event - switch back to Videos screen`() = runTest {
        underTest.test {
            val state = awaitItem()
            state.eventSink(Event.OnTopBarScreen(Screens.Account))

            awaitItem().currentScreen shouldBeEqualTo AccountScreen

            state.eventSink(Event.OnBack)

            awaitItem().currentScreen shouldBeEqualTo VideosScreen

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `event - finish`() = runTest {
        underTest.test {
            val state = awaitItem()
            state.eventSink(Event.OnBack)

            val screen = fakeNavigator.awaitPop().poppedScreen

            screen shouldBeEqualTo null

            ensureAllEventsConsumed()
        }
    }
}
