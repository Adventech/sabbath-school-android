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
import app.ss.models.AudioAux
import app.ss.models.PDFAux
import app.ss.models.VideoAux
import com.slack.circuit.retained.produceRetainedState
import io.adventech.blockkit.model.resource.Segment
import io.adventech.blockkit.model.resource.SegmentType
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import ss.document.components.DocumentTopAppBarAction
import ss.resources.api.ResourcesRepository
import javax.inject.Inject

@Stable
interface TopAppbarActionsProducer {

    @Composable
    operator fun invoke(
        resourceIndex: String,
        documentIndex: String,
        segment: Segment?
    ): ImmutableList<DocumentTopAppBarAction>
}

internal class TopAppbarActionsProducerImpl @Inject constructor(
    private val repository: ResourcesRepository,
) : TopAppbarActionsProducer {

    @Composable
    override fun invoke(
        resourceIndex: String,
        documentIndex: String,
        segment: Segment?
    ): ImmutableList<DocumentTopAppBarAction> {
        val audio by produceRetainedState<List<AudioAux>>(emptyList()) {
            value = repository.audio(resourceIndex, documentIndex).getOrNull().orEmpty()
        }
        val video by produceRetainedState<List<VideoAux>>(emptyList()) {
            value = repository.video(resourceIndex, documentIndex).getOrNull().orEmpty()
        }
        val pdfs by produceRetainedState<List<PDFAux>>(emptyList()) {
            value = repository.pdf(resourceIndex, documentIndex).getOrNull().orEmpty()
        }
        return buildList {
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
    }
}
