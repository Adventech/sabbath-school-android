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

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.slack.circuit.foundation.NavEvent
import io.adventech.blockkit.model.BlockData
import io.adventech.blockkit.model.resource.Segment
import io.adventech.blockkit.ui.input.UserInputState
import kotlinx.collections.immutable.ImmutableList
import ss.document.segment.SegmentUi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentPager(
    segments: ImmutableList<Segment>,
    titleBelowCover: Boolean,
    userInputState: UserInputState,
    modifier: Modifier = Modifier,
    initialPage: Int = 0,
    onPageChange: (Int) -> Unit = {},
    onNavBack: () -> Unit = {},
    onCollapseChange: (Boolean) -> Unit = {},
    onHandleUri: (String, BlockData?) -> Unit = { _, _ -> },
    onNavEvent: (NavEvent) -> Unit = {},
) {
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { segments.size },
    )

    LaunchedEffect(initialPage) { pagerState.animateScrollToPage(initialPage) }

    HorizontalPager(
        state = pagerState,
        modifier = modifier,
        verticalAlignment = Alignment.Top,
        beyondViewportPageCount = 1,
    ) { page ->
        val segment = segments[page]

        SegmentUi(
            segment = segment,
            titleBelowCover = titleBelowCover,
            userInputState = userInputState,
            modifier = Modifier,
            onNavBack = onNavBack,
            onCollapseChange = onCollapseChange,
            onHandleUri = onHandleUri,
            onNavEvent = onNavEvent,
        )
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            onPageChange(page)
        }
    }
}
