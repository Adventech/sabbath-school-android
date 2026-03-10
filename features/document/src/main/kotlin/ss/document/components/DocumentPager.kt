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

package ss.document.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.slack.circuit.foundation.NavEvent
import io.adventech.blockkit.model.BlockData
import io.adventech.blockkit.model.BlockItem
import io.adventech.blockkit.model.resource.ReferenceModel
import io.adventech.blockkit.model.resource.Segment
import io.adventech.blockkit.ui.input.UserInputState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.distinctUntilChanged
import ss.document.segment.SegmentUi

@Composable
fun DocumentPager(
    segments: ImmutableList<Segment>,
    titleBelowCover: Boolean,
    documentIndex: String,
    resourceIndex: String,
    documentId: String,
    userInputState: UserInputState,
    modifier: Modifier = Modifier,
    initialPage: Int = 0,
    onPageChange: (Int) -> Unit = {},
    onNavBack: () -> Unit = {},
    onCollapseChange: (Boolean) -> Unit = {},
    onHandleUri: (String, BlockData?) -> Unit = { _, _ -> },
    onHandleReference: (ReferenceModel) -> Unit = { _ -> },
    onFullScreenVideo: (BlockItem.Video) -> Unit = {},
    onNavEvent: (NavEvent) -> Unit = {},
) {
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { segments.size },
    )

    val pageListStateMap = remember { mutableMapOf<Int, LazyListState>() }

    // Define toolbar height threshold (Toolbar + Status Bar)
    val density = LocalDensity.current
    val topInsetPx = WindowInsets.statusBars.getTop(density)
    val toolbarHeightPx = with(density) { 56.dp.toPx() } + topInsetPx

    // Helper logic to determine collapse state based on scroll offset
    fun isCollapsed(listState: LazyListState): Boolean {
        val visibleItems = listState.layoutInfo.visibleItemsInfo
        val coverItem = visibleItems.find { it.index == 0 }

        return if (coverItem == null) {
            // If the cover (index 0) is not in the visible list, it's scrolled away -> Collapsed
            listState.firstVisibleItemIndex > 0
        } else {
            // If cover is visible, check if its bottom edge is above the toolbar
            // (coverItem.offset is usually negative as we scroll up)
            val coverBottom = coverItem.offset + coverItem.size
            coverBottom <= toolbarHeightPx
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize(),
        verticalAlignment = Alignment.Top,
        beyondViewportPageCount = 2,
    ) { page ->
        val segment = segments[page]

        val listState = rememberLazyListState()

        LaunchedEffect(listState) {
            snapshotFlow { isCollapsed(listState) }
                .distinctUntilChanged()
                .collect { collapsed ->
                    // Only update the callback if this is the currently displayed page
                    if (pagerState.currentPage == page) {
                        onCollapseChange(collapsed)
                    }
                    pageListStateMap[page] = listState
                }
        }

        SegmentUi(
            segment = segment,
            documentId = documentId,
            documentIndex = documentIndex,
            resourceIndex = resourceIndex,
            titleBelowCover = titleBelowCover,
            userInputState = userInputState,
            modifier = Modifier.fillMaxSize(),
            listState = listState,
            onNavBack = onNavBack,
            onCollapseChange = onCollapseChange,
            onHandleUri = onHandleUri,
            onHandleReference = onHandleReference,
            onNavEvent = onNavEvent,
            onFullScreenVideo = onFullScreenVideo,
        )
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            onPageChange(page)

            pageListStateMap[page]?.let { listState ->
                // Immediately check the state of the new page
                onCollapseChange(isCollapsed(listState))
            }
        }
    }

    LaunchedEffect(initialPage) { pagerState.scrollToPage(initialPage) }
}
