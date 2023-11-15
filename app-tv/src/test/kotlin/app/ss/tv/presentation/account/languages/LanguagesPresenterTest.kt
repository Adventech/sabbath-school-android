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

package app.ss.tv.presentation.account.languages

import app.ss.tv.data.model.LanguageSpec
import app.ss.tv.data.repository.FakeVideosRepository
import app.ss.tv.presentation.account.languages.LanguagesScreen.State
import com.slack.circuit.test.test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import ss.foundation.coroutines.test.TestDispatcherProvider
import ss.lessons.model.SSLanguage
import ss.prefs.api.test.FakeSSPrefs

/** Unit tests for [LanguagesPresenter]. */
class LanguagesPresenterTest {

    private val languagesFlow = MutableStateFlow("en")

    private val fakeRepository = FakeVideosRepository()
    private val fakePrefs = FakeSSPrefs(languagesFlow)

    private val languages = listOf(
        SSLanguage("fr", "French"),
        SSLanguage("en", "English")
    )

    private val underTest = LanguagesPresenter(
        repository = fakeRepository,
        ssPrefs = fakePrefs,
        dispatcherProvider = TestDispatcherProvider()
    )

    @Test
    fun `present - error state`() = runTest {
        underTest.test {
            awaitItem() shouldBeEqualTo State.Loading
            awaitItem() shouldBeEqualTo State.Error
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - languages state`() = runTest {
        fakeRepository.languagesResult = Result.success(languages)

        underTest.test {
            awaitItem() shouldBeEqualTo State.Loading

            (awaitItem() as State.Languages).languages shouldBeEqualTo persistentListOf(
                LanguageSpec("fr", "French", false),
                LanguageSpec("en", "English", true)
            )

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `event - OnSelected updates state`() = runTest {
        fakeRepository.languagesResult = Result.success(languages)
        val expected = persistentListOf(
            LanguageSpec("fr", "French", false),
            LanguageSpec("en", "English", true)
        )

        underTest.test {
            awaitItem() shouldBeEqualTo State.Loading

            var state = awaitItem() as State.Languages

            state.languages shouldBeEqualTo expected

            state.eventSink(LanguagesScreen.Event.OnSelected(expected.first()))

            state = awaitItem() as State.Languages

            state.languages shouldBeEqualTo persistentListOf(
                LanguageSpec("fr", "French", true),
                LanguageSpec("en", "English", false)
            )

            ensureAllEventsConsumed()
        }
    }
}
