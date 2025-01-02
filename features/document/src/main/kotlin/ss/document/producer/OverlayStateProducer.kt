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

package ss.document.producer

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import io.adventech.blockkit.model.BlockData
import ss.document.components.segment.overlay.ExcerptOverlay
import ss.document.producer.OverlayStateProducer.State
import timber.log.Timber
import javax.inject.Inject

@Stable
interface OverlayStateProducer {

    @Composable
    operator fun invoke(): State

    sealed interface State : CircuitUiState {

        data class None(
            val eventSink: (Event) -> Unit
        ) : State

        data class Excerpt(
            val state: ExcerptOverlay.State,
            val onResult: (ExcerptOverlay.Result) -> Unit
        ) : State
    }

    sealed interface Event : CircuitUiEvent {
        data class OnHandleUri(val uri: String, val data: BlockData?) : Event
    }
}

private const val SCHEME_BIBLE = "sspmBible"
private const val SCHEME_EGW = "sspmEGW"
private const val SCHEME_COMPLETION = "sspmCompletion"

class OverlayStateProducerImpl @Inject constructor() : OverlayStateProducer {

    @Composable
    override fun invoke(): State {
        var overlayState by rememberRetained { mutableStateOf<State?>(null) }
        val defaultState = State.None{ event ->
            when (event) {
                is OverlayStateProducer.Event.OnHandleUri -> {
                    val uri = Uri.parse(event.uri)
                    val data = event.data

                    when (uri.scheme) {
                        SCHEME_BIBLE -> {
                            val excerpt = data?.bible?.get(uri.host) ?: return@None

                            overlayState = State.Excerpt(ExcerptOverlay.State(excerpt = excerpt)) {
                                overlayState = null
                            }
                        }

                        SCHEME_EGW -> {
                            Timber.d("Handling egw uri: ${uri.host}")
                        }

                        SCHEME_COMPLETION -> {
                            Timber.d("Handling completion uri: ${uri.host}")
                        }
                    }
                }
            }
        }

        return overlayState ?: defaultState
    }

}
