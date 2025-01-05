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

package ss.segment

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.overlay.OverlayEffect
import dagger.hilt.components.SingletonComponent
import io.adventech.blockkit.model.resource.SegmentType
import ss.libraries.circuit.navigation.SegmentScreen
import ss.segment.components.blocks.SegmentBlocksContent
import ss.segment.components.overlay.BlocksOverlay
import ss.segment.components.overlay.ExcerptOverlay
import ss.segment.producer.OverlayStateProducer

@CircuitInject(SegmentScreen::class, SingletonComponent::class)
@Composable
fun SegmentUi(state: State, modifier: Modifier = Modifier) {
    when (state) {
        is State.Content -> {
            SegmentTypeContent(state, modifier)
        }
        State.Loading -> Unit
    }
}

@Composable
private fun SegmentTypeContent(state: State.Content, modifier: Modifier = Modifier) {
    val segment = state.segment
    when (segment.type) {
        SegmentType.UNKNOWN -> Unit
        SegmentType.BLOCK -> {
            SegmentBlocksContent(state, modifier)
        }
        SegmentType.STORY -> Unit
        SegmentType.PDF -> Unit
        SegmentType.VIDEO -> Unit
    }

    OverlayEffect(state.overlayState) {
        when (val state = state.overlayState) {
            is OverlayStateProducer.State.None -> Unit
            is OverlayStateProducer.State.Excerpt -> state.onResult(
                show(ExcerptOverlay(state.state))
            )
            is OverlayStateProducer.State.Blocks -> state.onResult(
                show(BlocksOverlay(state.state))
            )
        }
    }
}
