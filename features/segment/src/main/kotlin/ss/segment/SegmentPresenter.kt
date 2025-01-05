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

package ss.segment

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.components.SingletonComponent
import io.adventech.blockkit.model.resource.Segment
import ss.libraries.circuit.navigation.SegmentScreen
import ss.resources.api.ResourcesRepository
import ss.segment.producer.OverlayStateProducer
import ss.segment.producer.OverlayStateProducer.Event as OverlayEvent
import ss.segment.producer.OverlayStateProducer.State as OverlayState

class SegmentPresenter @AssistedInject constructor(
    @Assisted private val navigator: Navigator,
    @Assisted private val screen: SegmentScreen,
    private val repository: ResourcesRepository,
    private val overlayStateProducer: OverlayStateProducer,
) : Presenter<State> {

    @Composable
    override fun present(): State {
        val resource by rememberSegment()

        val overlayState = overlayStateProducer(navigator)

        val eventSink: (Event) -> Unit = { event ->
            when (event) {
                is Event.Blocks.OnHandleUri -> {
                    (overlayState as? OverlayState.None)?.eventSink(OverlayEvent.OnHandleUri(event.uri, event.data))
                }
            }
        }

        val segment = resource

        return when {
            segment == null -> State.Loading
            else -> State.Content(segment, screen.titleBelowCover, overlayState, eventSink)
        }
    }

    @Composable
    private fun rememberSegment() = produceRetainedState<Segment?>(initialValue = null) {
        repository.segment(screen.id)
            .collect {
                value = it.copy(cover = it.cover ?: screen.cover)
            }
    }

    @CircuitInject(SegmentScreen::class, SingletonComponent::class)
    @AssistedFactory
    interface Factory {
        fun create(navigator: Navigator, screen: SegmentScreen): SegmentPresenter
    }
}
