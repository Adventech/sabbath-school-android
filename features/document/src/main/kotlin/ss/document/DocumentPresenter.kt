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

package ss.document

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.foundation.onNavEvent
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.components.SingletonComponent
import io.adventech.blockkit.model.resource.ResourceDocument
import io.adventech.blockkit.model.resource.Segment
import io.adventech.blockkit.model.resource.SegmentType
import io.adventech.blockkit.ui.style.font.FontFamilyProvider
import kotlinx.collections.immutable.toImmutableList
import ss.document.producer.ReaderStyleStateProducer
import ss.document.producer.TopAppbarActionsProducer
import ss.libraries.circuit.navigation.DocumentScreen
import ss.resources.api.ResourcesRepository
import ss.document.producer.TopAppbarActionsState.Event as TopAppbarEvent

class DocumentPresenter @AssistedInject constructor(
    @Assisted private val navigator: Navigator,
    @Assisted private val screen: DocumentScreen,
    private val resourcesRepository: ResourcesRepository,
    private val actionsProducer: TopAppbarActionsProducer,
    private val fontFamilyProvider: FontFamilyProvider,
    private val readerStyleStateProducer: ReaderStyleStateProducer,
) : Presenter<State> {

    @Composable
    override fun present(): State {
        val response by rememberDocument()
        val documentPages by rememberDocumentSegments(response)
        var selectedPage by rememberRetained(documentPages) { mutableStateOf(documentPages.firstOrNull()) }

        val resourceDocument = response

        val actionsState = actionsProducer(screen.resourceId, screen.resourceIndex, screen.index, selectedPage)

        val overlayState = actionsState.overlayState // Assign other overlays

        val readerStyle = readerStyleStateProducer()

        val eventSink: (Event) -> Unit = { event ->
            when (event) {
                Event.OnNavBack -> navigator.pop()
                is Event.OnActionClick -> {
                    actionsState.eventSink(TopAppbarEvent.OnActionClick(event.action))
                }

                is SuccessEvent.OnPageChange -> {
                    selectedPage = documentPages.getOrNull(event.page)
                }

                is SuccessEvent.OnSegmentSelection -> {
                    selectedPage = event.segment
                }

                is SuccessEvent.OnNavEvent -> {
                    navigator.onNavEvent(event.event)
                }
            }
        }

        return when {
            resourceDocument == null -> State.Loading(screen.title, false, eventSink)
            else -> State.Success(
                title = selectedPage?.title ?: resourceDocument.title,
                hasCover = selectedPage?.hasCover() == true,
                actions = actionsState.actions,
                initialPage = documentPages.indexOf(selectedPage),
                segments = documentPages,
                selectedSegment = selectedPage,
                titleBelowCover = resourceDocument.titleBelowCover == true,
                style = resourceDocument.style,
                readerStyle = readerStyle,
                fontFamilyProvider = fontFamilyProvider,
                eventSink = eventSink,
                overlayState = overlayState
            )
        }
    }

    @Composable
    private fun rememberDocument() = produceRetainedState<ResourceDocument?>(null) {
        value = resourcesRepository.document(screen.index).getOrNull()
    }

    @Composable
    private fun rememberDocumentSegments(document: ResourceDocument?) = rememberRetained(document) {
        mutableStateOf(
            (document?.segments?.map { it.copy(cover = it.cover ?: screen.cover) } ?: emptyList())
                .toImmutableList()
        )
    }

    @CircuitInject(DocumentScreen::class, SingletonComponent::class)
    @AssistedFactory
    interface Factory {
        fun create(navigator: Navigator, screen: DocumentScreen): DocumentPresenter
    }
}

internal fun Segment.hasCover(): Boolean {
    return type == SegmentType.BLOCK && cover != null
}
