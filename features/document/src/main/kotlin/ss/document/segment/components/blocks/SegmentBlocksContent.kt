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

package ss.document.segment.components.blocks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.adventech.blockkit.model.BlockData
import io.adventech.blockkit.model.resource.ReferenceModel
import io.adventech.blockkit.model.resource.Segment
import io.adventech.blockkit.ui.BlockContent
import io.adventech.blockkit.ui.input.UserInputState
import io.adventech.blockkit.ui.style.LocalReaderStyle
import io.adventech.blockkit.ui.style.LocalSegmentStyle
import io.adventech.blockkit.ui.style.background
import io.adventech.blockkit.ui.style.primaryForeground
import ss.document.segment.components.SegmentCover
import ss.document.segment.components.SegmentHeader

@Composable
internal fun SegmentBlocksContent(
    segment: Segment,
    titleBelowCover: Boolean,
    modifier: Modifier = Modifier,
    userInputState: UserInputState,
    listState: LazyListState = rememberLazyListState(),
    onHandleUri: (String, BlockData?) -> Unit = { _, _ -> },
    onHandleReference: (ReferenceModel) -> Unit = { _ -> }
) {
    val readerStyle = LocalReaderStyle.current
    val contentColor = readerStyle.theme.primaryForeground()
    val segmentStyle = segment.style?.segment ?: LocalSegmentStyle.current

    // Stabilize the lambdas
    val stableOnHandleUri = remember(onHandleUri) { onHandleUri }
    val stableOnHandleReference = remember(onHandleReference) { onHandleReference }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(readerStyle.theme.background())
            .imePadding(),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(key = "cover-${segment.id}") {
            SegmentCover(
                cover = segment.cover,
                modifier = Modifier.animateItem(),
                headerContent = {
                    if (!(segment.titleBelowCover ?: titleBelowCover)) {
                        SegmentHeader(
                            title = segment.markdownTitle ?: segment.title,
                            subtitle = segment.markdownSubtitle ?: segment.subtitle,
                            date = segment.date,
                            contentColor = if (segment.cover != null) Color.White else contentColor,
                            style = segmentStyle.takeIf { segment.cover == null },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            )
        }

        if ((segment.titleBelowCover ?: titleBelowCover)) {
            item(key = "header-${segment.id}") {
                SegmentHeader(
                    title = segment.markdownTitle ?: segment.title,
                    subtitle = segment.markdownSubtitle ?: segment.subtitle,
                    date = segment.date,
                    contentColor = contentColor,
                    style = segmentStyle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem(),
                )
            }
        }


        // Using a single `item` with a `Column` to wrap the blocks avoids LazyColumn's
        // per-item recycling overhead. This prevents scroll jitter caused by the high
        // composition cost of individual `BlockContent` items.
        item(key = "blocks-container", contentType = "blocks-container") {
            Column(
                modifier = Modifier.animateItem(),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                segment.blocks.orEmpty().forEach { block ->
                    // We manually provide a key here for stability within the Column
                    key(block.id) {
                        BlockContent(
                            blockItem = block,
                            modifier = Modifier,
                            userInputState = userInputState,
                            onHandleUri = stableOnHandleUri,
                            onHandleReference = stableOnHandleReference,
                        )
                    }
                }
            }
        }

        item(key = "spacer") { Spacer(Modifier.height(48.dp)) }

        item(key = "spacer-system") { Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.systemBars)) }
    }
}
