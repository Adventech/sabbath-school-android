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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.components.SingletonComponent
import io.adventech.blockkit.model.feed.FeedGroup
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import ss.feed.group.FeedGroupScreen.Event
import ss.feed.group.FeedGroupScreen.State
import ss.feed.model.FeedResourceSpec
import ss.feed.model.toSpec
import ss.libraries.circuit.navigation.ResourceScreen
import ss.resources.api.ResourcesRepository

class FeedGroupPresenter @AssistedInject constructor(
    @Assisted private val screen: FeedGroupScreen,
    @Assisted private val navigator: Navigator,
    private val resourcesRepository: ResourcesRepository,
) : Presenter<State> {

    @Composable
    override fun present(): State {
        var title by rememberRetained { mutableStateOf(screen.title ?: "") }
        val feedGroup by produceRetainedState<FeedGroup?>(null) {
            value = resourcesRepository.feedGroup(screen.id, screen.feedType).getOrNull()?.also {
                title = it.title ?: ""
            }
        }

        val feedResources by rememberResources(feedGroup)

        val eventSink: (Event) -> Unit = { event ->
            when (event) {
                is Event.OnNavBack -> navigator.pop()
                is Event.OnItemClick -> navigator.goTo(ResourceScreen(event.index))
            }
        }

        return when {
            feedResources.isNotEmpty() -> State.Success(
                resources = feedResources,
                title = title,
                eventSink = eventSink
            )

            else -> State.Loading(
                title = title,
                eventSink = eventSink
            )
        }
    }

    @Composable
    private fun rememberResources(group: FeedGroup?) =
        rememberRetained(group) {
            mutableStateOf(
                group?.resources?.map {
                    it.toSpec(group, FeedResourceSpec.ContentDirection.HORIZONTAL)
                }?.toImmutableList() ?: persistentListOf()
            )
        }

    @CircuitInject(FeedGroupScreen::class, SingletonComponent::class)
    @AssistedFactory
    interface Factory {
        fun create(screen: FeedGroupScreen, navigator: Navigator): FeedGroupPresenter
    }
}
