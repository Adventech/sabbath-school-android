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

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
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
    listState : LazyListState = rememberLazyListState(),
    onHandleUri: (String, BlockData?) -> Unit = { _, _ -> },
    onHandleReference: (ReferenceModel) -> Unit = { _ -> }
) {
    val readerStyle = LocalReaderStyle.current
    val contentColor = readerStyle.theme.primaryForeground()
    val segmentStyle = segment.style?.segment ?: LocalSegmentStyle.current

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .background(readerStyle.theme.background()),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item(key = "cover-${segment.id}") {
            SegmentCover(
                cover = segment.cover,
                modifier = Modifier.animateItem(),
                headerContent = {
                    if ((segment.titleBelowCover ?: titleBelowCover) == false) {
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
                    modifier = Modifier.fillMaxWidth().animateItem(),
                )
            }
        }

        items(segment.blocks.orEmpty(), key = { it.id }) { block ->
            BlockContent(
                blockItem = block,
                modifier = Modifier.animateItem(
                    placementSpec = spring(
                        stiffness = Spring.StiffnessLow,
                        visibilityThreshold = IntOffset.VisibilityThreshold,
                    )
                ),
                userInputState = userInputState,
                onHandleUri = onHandleUri,
                onHandleReference = onHandleReference,
            )
        }

        item(key = "spacer") { Spacer(Modifier.height(48.dp)) }

        item(key = "spacer-system") { Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.systemBars)) }
    }
}
