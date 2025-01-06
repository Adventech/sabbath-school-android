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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import app.ss.models.AudioAux
import app.ss.models.PDFAux
import app.ss.models.VideoAux
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.CircuitUiState
import io.adventech.blockkit.model.resource.Segment
import io.adventech.blockkit.model.resource.SegmentType
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import ss.document.DocumentOverlayState
import ss.document.components.DocumentTopAppBarAction
import ss.resources.api.ResourcesRepository
import javax.inject.Inject

data class TopAppbarActionsState(
    val actions: ImmutableList<DocumentTopAppBarAction>,
    val overlayState: DocumentOverlayState?,
    val eventSink: (Event) -> Unit
): CircuitUiState {

    sealed interface Event {
        data class OnActionClick(val action: DocumentTopAppBarAction): Event
    }
}

@Stable
interface TopAppbarActionsProducer {

    @Composable
    operator fun invoke(
        resourceIndex: String,
        documentIndex: String,
        segment: Segment?
    ): TopAppbarActionsState
}

internal class TopAppbarActionsProducerImpl @Inject constructor(
    private val repository: ResourcesRepository,
) : TopAppbarActionsProducer {

    @Composable
    override fun invoke(
        resourceIndex: String,
        documentIndex: String,
        segment: Segment?
    ): TopAppbarActionsState {
        var bottomSheetState by rememberRetained { mutableStateOf<DocumentOverlayState?>(null) }

        val audio by produceRetainedState<List<AudioAux>>(emptyList()) {
            value = repository.audio(resourceIndex, documentIndex).getOrNull().orEmpty()
        }
        val video by produceRetainedState<List<VideoAux>>(emptyList()) {
            value = repository.video(resourceIndex, documentIndex).getOrNull().orEmpty()
        }
        val pdfs by produceRetainedState<List<PDFAux>>(emptyList()) {
            value = repository.pdf(resourceIndex, documentIndex).getOrNull().orEmpty()
        }
        val actions =  buildList {
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
                add(DocumentTopAppBarAction.DisplayOptions)
            }
        }.toImmutableList()

        return TopAppbarActionsState(
            actions = actions,
            overlayState = bottomSheetState,
            eventSink = { event ->
                when (event) {
                    is TopAppbarActionsState.Event.OnActionClick -> {
                        when (event.action) {
                            DocumentTopAppBarAction.Audio -> Unit
                            DocumentTopAppBarAction.Video -> Unit
                            DocumentTopAppBarAction.Pdf -> Unit
                            DocumentTopAppBarAction.DisplayOptions -> {
                                bottomSheetState = DocumentOverlayState.ReaderOptionsBottomSheet { result ->
                                    bottomSheetState = null
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}
