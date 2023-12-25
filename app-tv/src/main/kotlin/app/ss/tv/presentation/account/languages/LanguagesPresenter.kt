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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import app.ss.tv.data.model.LanguageSpec
import app.ss.tv.data.repository.VideosRepository
import app.ss.tv.presentation.account.languages.LanguagesScreen.Event
import app.ss.tv.presentation.account.languages.LanguagesScreen.State
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import ss.foundation.coroutines.DispatcherProvider
import ss.lessons.model.SSLanguage
import ss.prefs.api.SSPrefs
import timber.log.Timber

class LanguagesPresenter @AssistedInject constructor(
    private val repository: VideosRepository,
    private val ssPrefs: SSPrefs,
    private val dispatcherProvider: DispatcherProvider
) : Presenter<State> {

    @AssistedFactory
    interface Factory {
        fun create(): LanguagesPresenter
    }

    @Composable
    override fun present(): State {
        val result by produceRetainedState(Result.success(emptyList<SSLanguage>())) {
            repository.getLanguages().collect { value = it }
        }
        val selectedLanguage by produceRetainedState("en") {
            ssPrefs.getLanguageCodeFlow()
                .flowOn(dispatcherProvider.io)
                .catch { Timber.e(it) }
                .collect { value = it }
        }

        return when {
            result.isFailure -> State.Error
            result.getOrElse { emptyList() }.isEmpty() -> State.Loading
            else -> State.Languages(
                languages = result.toSpec(selectedLanguage)
            ) { event ->
                when (event) {
                    is Event.OnSelected -> ssPrefs.setLanguageCode(event.code)
                }
            }
        }
    }

    private fun Result<List<SSLanguage>>.toSpec(selected: String) = getOrThrow().map { (code, name) ->
        LanguageSpec(code, name, code == selected)
    }.toImmutableList()

}
