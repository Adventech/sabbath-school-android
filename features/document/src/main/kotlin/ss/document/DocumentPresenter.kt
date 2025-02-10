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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import app.ss.models.PDFAux
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.foundation.NavEvent
import com.slack.circuit.foundation.onNavEvent
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuitx.android.IntentScreen
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.components.SingletonComponent
import io.adventech.blockkit.model.ReferenceScope
import io.adventech.blockkit.model.resource.ResourceDocument
import io.adventech.blockkit.model.resource.Segment
import io.adventech.blockkit.model.resource.SegmentType
import io.adventech.blockkit.ui.style.font.FontFamilyProvider
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.joda.time.DateTime
import ss.document.components.DocumentTopAppBarAction
import ss.document.producer.ReaderStyleStateProducer
import ss.document.producer.TopAppbarActionsProducer
import ss.document.producer.TopAppbarActionsState
import ss.document.producer.UserInputStateProducer
import ss.document.segment.producer.SegmentOverlayStateProducer
import ss.libraries.circuit.navigation.DocumentScreen
import ss.libraries.circuit.navigation.ExpandedAudioPlayerScreen
import ss.libraries.circuit.navigation.PdfScreen
import ss.libraries.circuit.navigation.ResourceScreen
import ss.libraries.pdf.api.PdfReader
import ss.misc.DateHelper
import ss.resources.api.ResourcesRepository
import ss.document.DocumentOverlayState.Segment as SegmentOverlayState
import ss.document.producer.TopAppbarActionsState.Event as TopAppbarEvent
import ss.document.segment.producer.SegmentOverlayStateProducer.Event as SegmentOverlayEvent

class DocumentPresenter @AssistedInject constructor(
    @Assisted private val navigator: Navigator,
    @Assisted private val screen: DocumentScreen,
    private val resourcesRepository: ResourcesRepository,
    private val actionsProducer: TopAppbarActionsProducer,
    private val fontFamilyProvider: FontFamilyProvider,
    private val readerStyleStateProducer: ReaderStyleStateProducer,
    private val segmentOverlayStateProducer: SegmentOverlayStateProducer,
    private val userInputStateProducer: UserInputStateProducer,
    private val pdfReader: PdfReader,
) : Presenter<State> {

    private val today get() = DateTime.now().withTimeAtStartOfDay()

    @Composable
    override fun present(): State {
        val response by rememberDocument()
        val documentPages by rememberDocumentSegments(response)
        var selectedPage by rememberRetained(documentPages) { mutableStateOf(documentPages.defaultPage()) }

        val resourceDocument = response

        LaunchedEffect(resourceDocument) { checkPdfOnlySegment(resourceDocument) }

        val actionsState = resourceDocument?.let {
            actionsProducer(
                navigator = navigator,
                resourceId = it.resourceId,
                resourceIndex = it.resourceIndex,
                documentIndex = screen.index,
                documentId = resourceDocument.id,
                segment = selectedPage,
            )
        } ?: TopAppbarActionsState.Empty

        val actionsOverlayState = actionsState.overlayState
        val segmentOverlayState = segmentOverlayStateProducer(navigator)
        val overlayState = rememberRetained(actionsOverlayState, segmentOverlayState) { actionsOverlayState ?: segmentOverlayState }
        val userInputState = userInputStateProducer(documentId = resourceDocument?.id)

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
                    when (val event = event.event) {
                        is NavEvent.GoTo -> {
                            if (event.screen is ExpandedAudioPlayerScreen) {
                                actionsState.eventSink(TopAppbarEvent.OnActionClick(DocumentTopAppBarAction.Audio))
                            } else {
                                navigator.goTo(event.screen)
                            }
                        }
                        else -> navigator.onNavEvent(event)
                    }
                }

                is SuccessEvent.OnHandleUri -> {
                    val event = SegmentOverlayEvent.OnHandleUri(event.uri, event.data)
                    sendSegmentOverlayEvent(segmentOverlayState, event)
                }

                is SuccessEvent.OnHandleReference -> {
                    val (scope, segment, resource, document) = event.model

                    if (segment != null && resourceDocument != null && scope == ReferenceScope.SEGMENT) {
                        val event = SegmentOverlayEvent.OnHiddenSegment(
                            segment = segment,
                            documentId = resourceDocument.id,
                            documentIndex = resourceDocument.index,
                        )
                        sendSegmentOverlayEvent(segmentOverlayState, event)
                    } else if (document != null && scope == ReferenceScope.DOCUMENT) {
                        navigator.goTo(DocumentScreen(document.index, document.cover))
                    } else if (resource != null && scope == ReferenceScope.RESOURCE) {
                        navigator.goTo(ResourceScreen(resource.index))
                    }
                }
            }
        }

        return when {
            resourceDocument == null -> State.Loading(false, eventSink)
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
                documentId = resourceDocument.id,
                documentIndex = resourceDocument.index,
                resourceIndex = resourceDocument.resourceIndex,
                eventSink = eventSink,
                overlayState = overlayState,
                userInputState = userInputState,
            )
        }
    }

    @Composable
    private fun rememberDocument() = produceRetainedState<ResourceDocument?>(null) {
        resourcesRepository.document(screen.index).collect { value = it }
    }

    @Composable
    private fun rememberDocumentSegments(document: ResourceDocument?) = rememberRetained(document) {
        mutableStateOf(
            (document?.segments?.map { it.copy(cover = it.cover ?: screen.cover) } ?: emptyList())
                .toImmutableList()
        )
    }

    private fun ImmutableList<Segment>.defaultPage(): Segment? {
        forEachIndexed { index, segment ->
            val date = segment.date?.let { DateHelper.parseDate(it) }

            if (date?.isEqual(today) == true) {
                return segment
            }
        }
        return firstOrNull()
    }

    private fun checkPdfOnlySegment(resourceDocument: ResourceDocument?) {
        val document = resourceDocument ?: return
        val segments = document.segments ?: return
        val blocks = segments.flatMap { it.blocks.orEmpty() }
        val pdfs = segments.flatMap { it.pdf.orEmpty() }

        if (blocks.isEmpty() && pdfs.isNotEmpty()) {
            val pdfs = segments.flatMap { it.pdf.orEmpty() }
            val screen = PdfScreen(
                documentId = document.id,
                resourceId = document.resourceId,
                resourceIndex = document.resourceIndex,
                documentIndex = document.index,
                pdfs = pdfs.map {
                    PDFAux(
                        id = it.id,
                        src = it.src,
                        title = it.title,
                        target = it.target,
                        targetIndex = it.targetIndex,
                    )
                },
            )
            Snapshot.withMutableSnapshot {
                navigator.pop()
                navigator.goTo(IntentScreen(pdfReader.launchIntent(screen)))
            }
        }
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

internal fun sendSegmentOverlayEvent(overlayState: DocumentOverlayState, event: SegmentOverlayEvent) {
    when (val state = overlayState) {
        is SegmentOverlayState.None -> state.eventSink(event)
        else -> Unit
    }
}
