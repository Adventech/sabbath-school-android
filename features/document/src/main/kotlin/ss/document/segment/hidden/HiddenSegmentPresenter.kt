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

package ss.document.segment.hidden

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.foundation.onNavEvent
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.components.SingletonComponent
import io.adventech.blockkit.model.resource.ResourceDocument
import io.adventech.blockkit.model.resource.Segment
import io.adventech.blockkit.ui.style.font.FontFamilyProvider
import kotlinx.collections.immutable.toImmutableList
import ss.document.producer.ReaderStyleStateProducer
import ss.document.producer.UserInputStateProducer
import ss.document.segment.hidden.HiddenSegmentScreen.State
import ss.document.segment.hidden.HiddenSegmentScreen.Event
import ss.document.segment.producer.SegmentOverlayStateProducer
import ss.document.segment.producer.SegmentOverlayStateProducer.Event as SegmentOverlayEvent
import ss.document.sendSegmentOverlayEvent
import ss.resources.api.ResourcesRepository

class HiddenSegmentPresenter @AssistedInject constructor(
    @Assisted private val screen: HiddenSegmentScreen,
    @Assisted private val navigator: Navigator,
    private val fontFamilyProvider: FontFamilyProvider,
    private val resourcesRepository: ResourcesRepository,
    private val readerStyleStateProducer: ReaderStyleStateProducer,
    private val segmentOverlayStateProducer: SegmentOverlayStateProducer,
    private val userInputStateProducer: UserInputStateProducer,
) : Presenter<State> {

    @Composable
    override fun present(): State {
        val readerStyle = readerStyleStateProducer()
        val document by rememberDocument()
        val result by rememberSegment()
        val segmentOverlayState = segmentOverlayStateProducer(navigator)
        val userInputState = userInputStateProducer(documentId = document?.id)

        val segment = result

        return if (segment != null) {
            State.Success(
                title = segment.title,
                subtitle = segment.subtitle,
                date = segment.date,
                blocks = segment.blocks.orEmpty().toImmutableList(),
                style = document?.style,
                fontFamilyProvider = fontFamilyProvider,
                overlayState = segmentOverlayState,
                userInputState = userInputState,
                readerStyle = readerStyle,
                eventSink = { event ->
                    when (event) {
                        is Event.OnHandleUri -> {
                            sendSegmentOverlayEvent(segmentOverlayState, SegmentOverlayEvent.OnHandleUri(event.uri, event.data))
                        }
                        is Event.OnNavEvent -> navigator.onNavEvent(event.navEvent)
                    }
                }
            )
        } else {
            State.Loading(readerStyle)
        }
    }

    @Composable
    private fun rememberSegment() = produceRetainedState<Segment?>(null) {
        resourcesRepository.segment(screen.id, screen.index).collect { value = it }
    }

    @Composable
    private fun rememberDocument() = produceRetainedState<ResourceDocument?>(null) {
        value = resourcesRepository.document(screen.documentIndex).getOrNull()
    }

    @CircuitInject(HiddenSegmentScreen::class, SingletonComponent::class)
    @AssistedFactory
    interface Factory {
        fun create(screen: HiddenSegmentScreen, navigator: Navigator): HiddenSegmentPresenter
    }
}
