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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.core.net.toUri
import app.ss.models.PDFAux
import app.ss.models.media.AudioFile
import app.ss.models.media.SSVideo
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
import io.adventech.blockkit.model.BlockItem
import io.adventech.blockkit.model.ReferenceScope
import io.adventech.blockkit.model.resource.Resource
import io.adventech.blockkit.model.resource.ResourceDocument
import io.adventech.blockkit.model.resource.Segment
import io.adventech.blockkit.model.resource.SegmentType
import io.adventech.blockkit.ui.style.font.FontFamilyProvider
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
import ss.libraries.media.api.MediaNavigation
import ss.libraries.media.api.SSMediaPlayer
import ss.libraries.media.api.connectAndPlay
import ss.libraries.media.model.SSMediaItem
import ss.libraries.media.model.extensions.NONE_PLAYING
import ss.libraries.media.service.MusicService
import ss.libraries.media.service.VideoService
import ss.libraries.pdf.api.PdfReader
import ss.misc.DateHelper
import ss.resources.api.ResourcesRepository
import ss.services.media.ui.PlaybackConnection
import ss.services.media.ui.spec.PlaybackStateSpec
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
    private val playbackConnection: PlaybackConnection,
    private val mediaNavigation: MediaNavigation,
    private val mediaPlayer: SSMediaPlayer,
) : Presenter<State> {

    private val today get() = DateTime.now().withTimeAtStartOfDay()

    @Composable
    override fun present(): State {
        val coroutineScope = rememberCoroutineScope()
        val response by rememberDocument()
        val resource by rememberResource(response?.resourceIndex)
        val documentPages by rememberDocumentSegments(response)
        var selectedPage by rememberRetained(documentPages) { mutableStateOf(documentPages.defaultPage()) }

        val resourceDocument = response

       // LaunchedEffect(resourceDocument) { checkPdfOnlySegment(resourceDocument) }

        val actionsState = resourceDocument?.let {
            actionsProducer(
                navigator = navigator,
                resourceId = it.resourceId,
                resourceIndex = it.resourceIndex,
                documentIndex = screen.index,
                documentId = resourceDocument.id,
                segment = selectedPage,
                shareOptions = resourceDocument.share,
            )
        } ?: TopAppbarActionsState.Empty

        val userInputState = userInputStateProducer(documentId = resourceDocument?.id)
        val actionsOverlayState = actionsState.overlayState
        val segmentOverlayState = segmentOverlayStateProducer(navigator, userInputState)
        val overlayState = rememberRetained(actionsOverlayState, segmentOverlayState) { actionsOverlayState ?: segmentOverlayState }

        val readerStyle = readerStyleStateProducer()

        val eventSink: (Event) -> Unit = { event ->
            when (event) {
                Event.OnNavBack -> navigator.pop()
                is Event.OnActionClick -> {
                    actionsState.eventSink(TopAppbarEvent.OnActionClick(event.action, event.context))
                }

                is SuccessEvent.OnPageChange -> {
                    selectedPage = documentPages.getOrNull(event.page)
                }

                is SuccessEvent.OnSegmentSelection -> {
                    selectedPage = event.segment
                }

                is SuccessEvent.OnNavEvent -> {
                    when (val successEvent = event.event) {
                        is NavEvent.GoTo -> {
                            if (successEvent.screen is ExpandedAudioPlayerScreen) {
                                actionsState.eventSink(TopAppbarEvent.OnActionClick(DocumentTopAppBarAction.Audio, event.context))
                            } else {
                                navigator.goTo(successEvent.screen)
                            }
                        }
                        else -> navigator.onNavEvent(successEvent)
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
                        navigator.goTo(DocumentScreen(document.index))
                    } else if (resource != null && scope == ReferenceScope.RESOURCE) {
                        navigator.goTo(ResourceScreen(resource.index))
                    }
                }

                is SuccessEvent.OnFullScreenVideo -> {
                    val video = event.video.toSSVideo(resource, resourceDocument)

                    val intent = mediaNavigation.videoPlayer(
                        context = event.context,
                        video = video,
                        position = mediaPlayer.playbackProgress.value.currentPosition,
                    )
                    navigator.goTo(IntentScreen(intent))
                }

                is SuccessEvent.OnPlayVideo -> {
                    val video = event.video.toSSVideo(resource, resourceDocument)
                    coroutineScope.launch {
                        mediaPlayer.connectAndPlay(VideoService::class.java, SSMediaItem.Video(video))
                    }
                }

                is SuccessEvent.OnPlayAudio -> {
                    val audio = event.audio.toSSAudio(resource, resourceDocument)
                    coroutineScope.launch {
                        mediaPlayer.connectAndPlay(MusicService::class.java, SSMediaItem.Audio(audio, autoShowMiniPlayer = false))
                    }
                }
            }
        }

        return when {
            resourceDocument == null || documentPages.isEmpty() -> State.Loading(selectedPage?.hasCover() == true, eventSink)
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
                isMiniPlayerVisible = isMiniPlayerVisible(),
                mediaPlayer = mediaPlayer,
            )
        }
    }

    @Composable
    private fun isMiniPlayerVisible(): Boolean {
        val playbackState by produceRetainedState(PlaybackStateSpec.NONE) {
            playbackConnection.playbackState.collect { value = it }
        }
        val nowPlaying by produceRetainedState(NONE_PLAYING) {
            playbackConnection.nowPlaying.collect { value = it }
        }
        
        return (playbackState != PlaybackStateSpec.NONE &&
            nowPlaying != NONE_PLAYING) &&
            playbackState.canShowMini
    }

    @Composable
    private fun rememberDocument() = produceRetainedState<ResourceDocument?>(null) {
        resourcesRepository.document(screen.index).collect { value = it }
    }

    @Composable
    private fun rememberResource(index: String?) = produceRetainedState<Resource?>(null, key1 = index) {
        index?.let {
            resourcesRepository.resource(index = index, cacheOnly = true).collect { value = it }
        }
    }

    @Composable
    private fun rememberDocumentSegments(document: ResourceDocument?) =
        produceRetainedState(persistentListOf(), document) {
            if (value == persistentListOf<Segment>()) {
                delay(150)
            }
            value = (document?.segments?.map {
                it.copy(
                    cover = it.cover ?: document.cover,
                    background = it.background ?: document.background,
                )
            } ?: emptyList())
                .toImmutableList()
    }

    private fun ImmutableList<Segment>.defaultPage(): Segment? {
        // 1. Check for index and return immediately if found.
        val indexedSegment = screen.segmentIndex
            ?.toIntOrNull()
            ?.let { getOrNull(it) }

        if (indexedSegment != null) {
            return indexedSegment
        }

        // 2. Fallback to today's date or the first segment.
        return firstOrNull { segment ->
            val date = segment.date?.let { DateHelper.parseDate(it) }
            date?.isEqual(today) == true
        } ?: firstOrNull()
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
                segmentId = null,
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
            //    navigator.pop()
               // navigator.goTo(IntentScreen(pdfReader.launchIntent(screen)))
            }
        }
    }

    private fun BlockItem.Video.toSSVideo(
        resource: Resource?,
        document: ResourceDocument?,
    ): SSVideo = SSVideo(
        artist = resource?.title.orEmpty(),
        id = id,
        src = src,
        title = caption ?: document?.title.orEmpty(),
        target = "",
        targetIndex = "",
        thumbnail = document?.cover ?: resource?.covers?.landscape.orEmpty(),
        hls = if (src.contains(".m3u8", true)) src else null,
    )

    private fun BlockItem.Audio.toSSAudio(
        resource: Resource?,
        document: ResourceDocument?,
    ): AudioFile = AudioFile(
        id = id,
        title = caption ?: document?.title.orEmpty(),
        artist = resource?.title.orEmpty(),
        source = src.toUri(),
        image = document?.cover ?: resource?.covers?.landscape.orEmpty(),
    )


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
    when (overlayState) {
        is SegmentOverlayState.None -> overlayState.eventSink(event)
        else -> Unit
    }
}
