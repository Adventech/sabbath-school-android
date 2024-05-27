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

package app.ss.quarterlies.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import app.ss.quarterlies.QuarterliesUseCase
import app.ss.quarterlies.list.QuarterliesListScreen.Event
import app.ss.quarterlies.list.QuarterliesListScreen.State
import app.ss.quarterlies.model.GroupedQuarterlies
import app.ss.quarterlies.model.placeHolderQuarterlies
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import ss.lessons.api.repository.QuarterliesRepository
import ss.libraries.circuit.navigation.LessonsScreen
import ss.prefs.api.SSPrefs
import timber.log.Timber

class QuarterliesListPresenter @AssistedInject constructor(
    @Assisted private val navigator: Navigator,
    @Assisted private val screen: QuarterliesListScreen,
    private val repository: QuarterliesRepository,
    private val ssPrefs: SSPrefs,
    private val quarterliesUseCase: QuarterliesUseCase
) : Presenter<State> {

    @CircuitInject(QuarterliesListScreen::class, SingletonComponent::class)
    @AssistedFactory
    interface Factory {
        fun create(navigator: Navigator, screen: QuarterliesListScreen): QuarterliesListPresenter
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Composable
    override fun present(): State {
        val quarterlies by produceRetainedState<GroupedQuarterlies>(
            initialValue = GroupedQuarterlies.TypeList(placeHolderQuarterlies())
        ) {
            ssPrefs.getLanguageCodeFlow()
                .flatMapLatest { language -> repository.getQuarterlies(language, screen.quarterlyGroup) }
                .map(quarterliesUseCase::group)
                .catch { Timber.e(it) }
                .collect { value = it }
        }

        return State(
            title = screen.quarterlyGroup.name,
            type = quarterlies,
            eventSink = { event ->
                when (event) {
                    is Event.OnNavBack -> navigator.pop()
                    is Event.QuarterlySelected -> navigator.goTo(LessonsScreen(event.index))
                }
            }
        )
    }
}
