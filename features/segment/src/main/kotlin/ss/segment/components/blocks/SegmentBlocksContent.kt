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

package ss.segment.components.blocks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.ss.design.compose.extensions.modifier.thenIf
import app.ss.design.compose.theme.SsTheme
import io.adventech.blockkit.model.BlockData
import io.adventech.blockkit.model.resource.Segment
import io.adventech.blockkit.ui.BlockContent
import io.adventech.blockkit.ui.style.LocalReaderStyle
import io.adventech.blockkit.ui.style.LocalSegmentStyle
import io.adventech.blockkit.ui.style.background
import ss.segment.Event
import ss.segment.State
import ss.segment.components.SegmentCover
import ss.segment.components.SegmentHeader
import kotlin.collections.forEach
import kotlin.collections.orEmpty

@Composable
internal fun SegmentBlocksContent(state: State.Content, modifier: Modifier = Modifier) {
    val readerStyle = LocalReaderStyle.current
    val segment = state.segment

    Column(modifier = modifier.fillMaxWidth()) {
        SegmentCover(
            cover = segment.cover,
            headerContent = { dominantColor ->
                if (state.titleBelowCover == false) {
                    val gradient = remember(dominantColor) {
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(0.1f),
                                dominantColor,
                            )
                        )
                    }

                    SegmentHeader(
                        title = segment.title,
                        subtitle = segment.subtitle,
                        date = segment.date,
                        contentColor = if (segment.cover != null) Color.White else SsTheme.colors.primaryForeground,
                        style = LocalSegmentStyle.current.takeIf { segment.cover == null },
                        modifier = Modifier
                            .fillMaxWidth()
                            .thenIf(segment.cover != null) {
                                background(gradient)
                            }
                    )
                }
            }
        )

        if (state.titleBelowCover) {
            SegmentHeader(
                title = segment.title,
                subtitle = segment.subtitle,
                date = segment.date,
                contentColor = SsTheme.colors.primaryForeground,
                style = LocalSegmentStyle.current,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(readerStyle.theme.background())
            )
        }

        SegmentBlockView(segment) { uri, data ->
            state.eventSink(Event.Blocks.OnHandleUri(uri, data))
        }
    }
}

@Composable
private fun SegmentBlockView(
    segment: Segment,
    modifier: Modifier = Modifier,
    onHandleUri: (String, BlockData?) -> Unit = { _, _ -> },
) {
    val readerStyle = LocalReaderStyle.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(readerStyle.theme.background())
            .padding(vertical = SsTheme.dimens.grid_4),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        segment.blocks.orEmpty().forEach { block ->
            BlockContent(block, Modifier, onHandleUri = onHandleUri)
        }

        Spacer(Modifier.fillMaxWidth().height(SsTheme.dimens.grid_4))
    }
}
