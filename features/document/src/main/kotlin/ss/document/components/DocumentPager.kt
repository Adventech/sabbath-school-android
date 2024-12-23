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

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import app.ss.models.resource.Segment
import app.ss.models.resource.SegmentType
import kotlinx.collections.immutable.ImmutableList
import ss.document.components.segment.SegmentBlockView
import ss.document.components.segment.SegmentCover

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentPager(
    segments: ImmutableList<Segment>,
    selectedSegment: Segment?,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    onPageChange: (Int) -> Unit = {},
) {
    val pagerState = rememberPagerState(
        pageCount = { segments.size },
    )

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        state = listState,
    ) {
        item("cover") {
            selectedSegment?.let {
                SegmentCover(cover = selectedSegment.cover)
            }
        }
        item {
            HorizontalPager(
                state = pagerState,
                modifier = modifier.fillMaxSize(),
            ) { page ->

                val segment = segments[page]
                when (segment.type) {
                    SegmentType.UNKNOWN -> Unit
                    SegmentType.BLOCK -> SegmentBlockView(segment)
                    SegmentType.STORY -> Unit
                    SegmentType.PDF -> Unit
                    SegmentType.VIDEO -> Unit
                }
            }

            LaunchedEffect(pagerState) {
                snapshotFlow { pagerState.currentPage }.collect { page ->
                    onPageChange(page)
                }
            }
        }
    }
}
