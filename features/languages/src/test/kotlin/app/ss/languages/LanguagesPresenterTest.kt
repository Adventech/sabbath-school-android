/*
 * Copyright (c) 2025. Adventech <info@adventech.io>
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

package app.ss.languages

import app.ss.languages.state.LanguageUiModel
import app.ss.languages.state.LanguagesEvent
import app.ss.languages.state.State
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.test
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.Test
import ss.libraries.appwidget.api.FakeAppWidgetHelper
import ss.libraries.circuit.navigation.HomeNavScreen
import ss.libraries.circuit.navigation.LanguagesScreen
import ss.prefs.api.test.FakeSSPrefs
import ss.resources.api.test.FakeResourcesRepository
import ss.resources.model.LanguageModel

/** Tests for [LanguagesPresenter]. */
class LanguagesPresenterTest {

    private val languagesFlow = MutableSharedFlow<List<LanguageModel>>()
    private val fakeNavigator = FakeNavigator(LanguagesScreen)
    private val fakeRepository = FakeResourcesRepository(languagesFlow)
    private val fakeSSPrefs = FakeSSPrefs(MutableStateFlow("en"))
    private val fakeAppWidgetHelper = FakeAppWidgetHelper()

    private val underTest =
        LanguagesPresenter(
            navigator = fakeNavigator,
            repository = fakeRepository,
            ssPrefs = fakeSSPrefs,
            appWidgetHelper = fakeAppWidgetHelper,
        )

    @Test
    fun `present - emit models with properly formatted native language name`() = runTest {
        underTest.test {
            awaitItem() shouldBeInstanceOf State.Loading::class.java

            languagesFlow.emit(
                listOf(
                    LanguageModel("en", "English", "English"),
                    LanguageModel("es", "Spanish", "Español"),
                    LanguageModel("fr", "French", "Français"),
                )
            )

            val models = (awaitItem() as State.Languages).models
            models.toList() shouldBeEqualTo
                listOf(
                    LanguageUiModel("en", "English", "English", true),
                    LanguageUiModel("es", "Spanish", "Español", false),
                    LanguageUiModel("fr", "French", "Français", false),
                )

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - event - Search`() = runTest {
        val query = "english"
        val result = listOf(LanguageModel("en", "English", "English"))

        underTest.test {
            awaitItem() shouldBeInstanceOf State.Loading::class.java

            languagesFlow.emit(emptyList())

            var state = awaitItem() as State.Languages

            state.eventSink(LanguagesEvent.Search(query))

            languagesFlow.emit(result)

            state = awaitItem() as State.Languages

            state.models.first().code shouldBeEqualTo "en"

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - event - Search - trim query`() = runTest {
        val result = listOf(LanguageModel("hello", "English", "English"))

        underTest.test {
            awaitItem() shouldBeInstanceOf State.Loading::class.java

            languagesFlow.emit(emptyList())

            var state = awaitItem() as State.Languages

            state.eventSink(LanguagesEvent.Search("   hello   "))

            languagesFlow.emit(result)

            state = awaitItem() as State.Languages

            state.models.first().code shouldBeEqualTo "hello"

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - event - Select`() = runTest {
        underTest.test {
            awaitItem() shouldBeInstanceOf State.Loading::class.java

            languagesFlow.emit(emptyList())

            val state = awaitItem() as State.Languages

            state.eventSink(LanguagesEvent.Select(LanguageUiModel("es", "Spanish", "Español", false)))

            fakeSSPrefs.setLanguageCode shouldBeEqualTo "es"

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - event - Select - language changed`() = runTest {
        fakeSSPrefs.setLanguageCode("en")

        underTest.test {
            awaitItem() shouldBeInstanceOf State.Loading::class.java

            languagesFlow.emit(emptyList())

            val state = awaitItem() as State.Languages

            state.eventSink(LanguagesEvent.Select(LanguageUiModel("es", "Spanish", "Español", false)))

            fakeSSPrefs.setLanguageCode shouldBeEqualTo "es"

            fakeAppWidgetHelper.isLanguageChangedCalled shouldBeEqualTo true

            fakeNavigator.awaitResetRoot().newRoot shouldBeEqualTo HomeNavScreen

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - event - Select - no language change`() = runTest {
        fakeSSPrefs.setLanguageCode("en")

        underTest.test {
            awaitItem() shouldBeInstanceOf State.Loading::class.java

            languagesFlow.emit(emptyList())

            val state = awaitItem() as State.Languages

            state.eventSink(LanguagesEvent.Select(LanguageUiModel("en", "English", "English", false)))

            fakeSSPrefs.setLanguageCode shouldBeEqualTo "en"

            fakeNavigator.awaitPop().result shouldBeEqualTo null

            ensureAllEventsConsumed()
        }
    }
}
