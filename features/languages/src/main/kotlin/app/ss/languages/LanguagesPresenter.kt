/*
 * Copyright (c) 2024. Adventech <info@adventech.io>
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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.ss.languages.state.Event
import app.ss.languages.state.LanguageUiModel
import app.ss.languages.state.LanguagesEvent
import app.ss.languages.state.State
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.components.SingletonComponent
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import ss.libraries.circuit.navigation.LanguagesScreen
import ss.libraries.circuit.navigation.QuarterliesScreen
import ss.prefs.api.SSPrefs
import ss.resources.api.ResourcesRepository
import ss.resources.model.LanguageModel

class LanguagesPresenter
@AssistedInject
constructor(
    @Assisted private val navigator: Navigator,
    private val repository: ResourcesRepository,
    private val ssPrefs: SSPrefs,
) : Presenter<State> {

    @CircuitInject(LanguagesScreen::class, SingletonComponent::class)
    @AssistedFactory
    interface Factory {
        fun create(navigator: Navigator): LanguagesPresenter
    }

    @Composable
    override fun present(): State {
        var query by rememberRetained { mutableStateOf<String?>(null) }
        val viewModels by
        produceRetainedState<ImmutableList<LanguageUiModel>?>(
            initialValue = null,
            key1 = query,
        ) {
            repository.languages(query)
                .collect { value = it.toModels() }
        }

        return when (val models = viewModels) {
            null ->
                State.Loading { event ->
                    when (event) {
                        Event.NavBack -> navigator.pop()
                    }
                }

            else ->
                State.Languages(models) { event ->
                    when (event) {
                        is LanguagesEvent.Select -> {
                            if (modelSelected(event.model)) {
                                navigator.resetRoot(QuarterliesScreen())
                            } else {
                                navigator.pop()
                            }
                        }

                        LanguagesEvent.NavBack -> navigator.pop()
                        is LanguagesEvent.Search -> query = event.query?.trim()
                    }
                }
        }
    }

    private fun modelSelected(model: LanguageUiModel): Boolean {
        val languageChanged = model.code != ssPrefs.getLanguageCode()
        ssPrefs.setLanguageCode(model.code)
        ssPrefs.setLastQuarterlyIndex(null)
        return languageChanged
    }

    private fun List<LanguageModel>.toModels() =
        map {
            LanguageUiModel(
                code = it.code,
                nativeName = it.nativeName,
                name = it.name,
                selected = it.code == ssPrefs.getLanguageCode(),
            )
        }.toImmutableList()
}
