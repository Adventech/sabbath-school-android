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

package ss.document.producer

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.ss.models.PDFAux
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuitx.android.IntentScreen
import dagger.Lazy
import io.adventech.blockkit.model.resource.Segment
import io.adventech.blockkit.model.resource.SegmentType
import io.adventech.blockkit.model.resource.ShareGroup
import io.adventech.blockkit.model.resource.ShareOptions
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import ss.document.DocumentOverlayState
import ss.document.DocumentOverlayState.BottomSheet
import ss.document.components.DocumentTopAppBarAction
import ss.document.reader.ReaderOptionsScreen
import ss.foundation.android.intent.ShareIntentHelper
import ss.libraries.circuit.navigation.AudioPlayerScreen
import ss.libraries.circuit.navigation.PdfScreen
import ss.libraries.circuit.navigation.ShareOptionsScreen
import ss.libraries.circuit.navigation.VideosScreen
import ss.libraries.pdf.api.PdfReader
import ss.resources.api.ResourcesRepository
import javax.inject.Inject

data class TopAppbarActionsState(
    val actions: ImmutableList<DocumentTopAppBarAction>,
    val overlayState: DocumentOverlayState?,
    val eventSink: (Event) -> Unit
) : CircuitUiState {

    sealed interface Event {
        data class OnActionClick(val action: DocumentTopAppBarAction, val context: Context) : Event
    }

    companion object {
        val Empty = TopAppbarActionsState(
            actions = persistentListOf(),
            overlayState = null,
            eventSink = {}
        )
    }
}

@Stable
interface TopAppbarActionsProducer {

    @Composable
    operator fun invoke(
        navigator: Navigator,
        resourceId: String,
        resourceIndex: String,
        documentIndex: String,
        documentId: String,
        segment: Segment?,
        shareOptions: ShareOptions?,
    ): TopAppbarActionsState
}

internal class TopAppbarActionsProducerImpl @Inject constructor(
    private val repository: ResourcesRepository,
    private val pdfReader: PdfReader,
    private val shareIntentHelper: Lazy<ShareIntentHelper>,
) : TopAppbarActionsProducer {

    @Composable
    override fun invoke(
        navigator: Navigator,
        resourceId: String,
        resourceIndex: String,
        documentIndex: String,
        documentId: String,
        segment: Segment?,
        shareOptions: ShareOptions?,
    ): TopAppbarActionsState {
        var bottomSheetState by rememberRetained { mutableStateOf<DocumentOverlayState?>(null) }

        val audio by produceRetainedState(emptyList()) {
            value = repository.audio(resourceIndex, documentIndex).getOrNull().orEmpty()
        }
        val video by produceRetainedState(emptyList()) {
            value = repository.video(resourceIndex, documentIndex).getOrNull().orEmpty()
        }
        val pdfs by produceRetainedState(emptyList()) {
            if (segment?.type == SegmentType.PDF) return@produceRetainedState
            value = repository.pdf(resourceIndex, documentIndex).getOrNull().orEmpty()
        }
        val actions = remember(audio, video, pdfs, segment, shareOptions) {
            buildList {
                if (audio.isNotEmpty()) {
                    add(DocumentTopAppBarAction.Audio)
                }
                if (video.isNotEmpty()) {
                    add(DocumentTopAppBarAction.Video)
                }
                if (segment?.type == SegmentType.BLOCK) {
                    if (pdfs.isNotEmpty()) {
                        add(DocumentTopAppBarAction.Pdf)
                    }

                    if (shareOptions != null) {
                        add(DocumentTopAppBarAction.Share)
                    }

                    add(DocumentTopAppBarAction.DisplayOptions)
                }
            }.toImmutableList()
        }

        return TopAppbarActionsState(
            actions = actions,
            overlayState = bottomSheetState,
            eventSink = { event ->
                when (event) {
                    is TopAppbarActionsState.Event.OnActionClick -> {
                        when (event.action) {
                            DocumentTopAppBarAction.Audio -> {
                                bottomSheetState = BottomSheet(
                                    screen = AudioPlayerScreen(resourceId, segment?.id),
                                    skipPartiallyExpanded = true,
                                    themed = false,
                                    feedback = false,
                                ) { _ ->
                                    bottomSheetState = null
                                }
                            }
                            DocumentTopAppBarAction.Video -> {
                                bottomSheetState = BottomSheet(
                                    screen = VideosScreen(documentIndex, documentId),
                                    skipPartiallyExpanded = true,
                                    themed = false,
                                    feedback = false,
                                ) { _ ->
                                    bottomSheetState = null
                                }
                            }
                            DocumentTopAppBarAction.Pdf -> {
                                val screen = PdfScreen(
                                    documentId = documentId,
                                    resourceId = resourceId,
                                    documentIndex = documentIndex,
                                    resourceIndex = resourceIndex,
                                    segmentId = segment?.id,
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
                                navigator.goTo(IntentScreen(pdfReader.launchIntent(screen)))
                            }
                            DocumentTopAppBarAction.DisplayOptions -> {
                                bottomSheetState = BottomSheet(
                                    screen = ReaderOptionsScreen,
                                    skipPartiallyExpanded = false,
                                    themed = false,
                                    feedback = false,
                                ) { _ ->
                                    bottomSheetState = null
                                }
                            }
                            DocumentTopAppBarAction.Share -> {
                                val shareGroups = shareOptions?.shareGroups ?: return@TopAppbarActionsState
                                val linkGroup = shareGroups.firstOrNull()
                                if (shareGroups.size == 1 && linkGroup is ShareGroup.Link && linkGroup.links.size == 1) {
                                    val shareLink = linkGroup.links.first().src
                                    shareIntentHelper.get().shareText(event.context, shareLink)
                                } else {
                                    bottomSheetState = BottomSheet(
                                        screen = ShareOptionsScreen(
                                            options = shareOptions,
                                            title = segment?.title ?: "",
                                            resourceColor = null,
                                        ),
                                        skipPartiallyExpanded = false,
                                        themed = false,
                                        feedback = false,
                                    ) { _ ->
                                        bottomSheetState = null
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}
