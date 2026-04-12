/*
 * Copyright (c) 2026. Adventech <info@adventech.io>
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

package ss.document.segment.components.pdf

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import app.ss.models.media.MediaAvailability
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.runtime.CircuitUiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import ss.document.components.DocumentTopAppBarAction
import ss.libraries.circuit.navigation.PdfScreen
import ss.resources.api.ResourcesRepository
import javax.inject.Inject

@Stable
interface PdfTopAppBarStateProducer {
    @Composable
    operator fun invoke(screen: PdfScreen): PdfTopAppBarState
}

@Immutable
data class PdfTopAppBarState(
    val actions: ImmutableList<DocumentTopAppBarAction>,
): CircuitUiState

class PdfTopAppBarStateProducerImpl @Inject constructor(
    private val resourcesRepository: ResourcesRepository,
) : PdfTopAppBarStateProducer {
    @Composable
    override fun invoke(screen: PdfScreen): PdfTopAppBarState {
        val mediaAvailability by rememberMediaAvailability(screen)

        val actions by produceRetainedState(persistentListOf(), mediaAvailability)  {
            value = buildList {
                if (mediaAvailability.audio) {
                    add(DocumentTopAppBarAction.Audio)
                }
                if (mediaAvailability.video) {
                    add(DocumentTopAppBarAction.Video)
                }
                add(DocumentTopAppBarAction.Annotations)
                add(DocumentTopAppBarAction.Outline)
                add(DocumentTopAppBarAction.Settings)
            }.toImmutableList()
        }

        return PdfTopAppBarState(actions)
    }

    @Composable
    private fun rememberMediaAvailability(screen: PdfScreen): State<MediaAvailability> = produceRetainedState(MediaAvailability()) {
        val documentIndex = screen.documentIndex
        val resourceIndex = screen.resourceIndex

        val audioDeferred = contentDeferred { resourcesRepository.audio(resourceIndex, documentIndex) }
        val videoDeferred = contentDeferred { resourcesRepository.video(resourceIndex, documentIndex) }

        value = MediaAvailability(
            audio = audioDeferred.await(),
            video = videoDeferred.await(),
        )
    }

    private fun <T> CoroutineScope.contentDeferred(content: suspend () -> Result<List<T>>): Deferred<Boolean> {
        return async { content().getOrDefault(emptyList()).isNotEmpty() }
    }
}
