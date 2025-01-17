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

package ss.document.segment.producer

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.Navigator
import io.adventech.blockkit.model.BlockData
import kotlinx.collections.immutable.toImmutableList
import ss.document.producer.ReaderStyleStateProducer
import ss.document.segment.components.overlay.BlocksOverlay
import ss.document.segment.components.overlay.ExcerptOverlay
import ss.libraries.circuit.navigation.CustomTabsIntentScreen
import timber.log.Timber
import javax.inject.Inject
import ss.document.DocumentOverlayState.Segment as SegmentOverlayState

@Stable
interface SegmentOverlayStateProducer {

    @Composable
    operator fun invoke(navigator: Navigator): SegmentOverlayState

    sealed interface Event : CircuitUiEvent {
        data class OnHandleUri(val uri: String, val data: BlockData?) : Event
    }
}

private const val SCHEME_BIBLE = "sspmBible"
private const val SCHEME_EGW = "sspmEGW"
private const val SCHEME_COMPLETION = "sspmCompletion"
private val WEB_SCHEMES = setOf("http", "https", null)

internal class OverlayStateProducerImpl @Inject constructor(
    private val readerStyleStateProducer: ReaderStyleStateProducer,
) : SegmentOverlayStateProducer {

    @Composable
    override fun invoke(navigator: Navigator): SegmentOverlayState {
        var overlayState by rememberRetained { mutableStateOf<SegmentOverlayState?>(null) }
        val readerStyle = readerStyleStateProducer()

        val defaultState = SegmentOverlayState.None { event ->
            when (event) {
                is SegmentOverlayStateProducer.Event.OnHandleUri -> {
                    val uri = Uri.parse(event.uri)
                    val data = event.data

                    when (uri.scheme) {
                        SCHEME_BIBLE -> {
                            val excerpt = data?.bible?.get(uri.host) ?: return@None

                            overlayState = SegmentOverlayState.Excerpt(
                                ExcerptOverlay.State(
                                    excerpt = excerpt,
                                    style = readerStyle,
                                )
                            ) {
                                overlayState = null
                            }
                        }

                        SCHEME_EGW -> {
                            val blocks = data?.egw?.get(uri.host) ?: return@None
                            overlayState = SegmentOverlayState.Blocks(state = BlocksOverlay.State(blocks.toImmutableList(), readerStyle)) {
                                overlayState = null
                            }
                        }

                        SCHEME_COMPLETION -> {
                            Timber.d("Handling completion uri: ${uri.host}")
                        }

                        in WEB_SCHEMES -> {
                            navigator.goTo(CustomTabsIntentScreen(event.uri))
                        }
                    }
                }
            }
        }

        return overlayState ?: defaultState
    }

}
