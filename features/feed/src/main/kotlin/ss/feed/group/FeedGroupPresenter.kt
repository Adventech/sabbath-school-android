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

package ss.feed.group

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import app.ss.models.feed.FeedGroup
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.components.SingletonComponent
import ss.feed.group.FeedGroupScreen.Event
import ss.feed.group.FeedGroupScreen.State
import ss.resources.api.ResourcesRepository

class FeedGroupPresenter @AssistedInject constructor(
    @Assisted private val screen: FeedGroupScreen,
    @Assisted private val navigator: Navigator,
    private val resourcesRepository: ResourcesRepository,
) : Presenter<State> {

    @Composable
    override fun present(): State {
        var title by rememberRetained { mutableStateOf(screen.title) }
        val group by produceRetainedState<FeedGroup?>(null) {
            value = resourcesRepository.feedGroup(screen.id, screen.feedType).getOrNull()?.also {
                title = it.title
            }
        }

        val eventSink: (Event) -> Unit = { event ->
            when (event) {
                is Event.OnNavBack -> navigator.pop()
                is Event.OnItemClick -> {
                    // Handle item click
                }
            }
        }

        val feedGroup = group

        return when {
            feedGroup != null -> State.Success(
                title = title,
                feedGroup = feedGroup,
                eventSink = eventSink
            )
            else -> State.Loading(
                title = title,
                eventSink = eventSink
            )
        }
    }

    @CircuitInject(FeedGroupScreen::class, SingletonComponent::class)
    @AssistedFactory
    interface Factory {
        fun create(screen: FeedGroupScreen, navigator: Navigator): FeedGroupPresenter
    }
}
