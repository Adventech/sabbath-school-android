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

package ss.document.components.segment

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.ss.design.compose.extensions.scroll.rememberScrollAlpha
import app.ss.design.compose.theme.SsTheme
import app.ss.models.resource.Segment

@Composable
fun SegmentBlockView(
    segment: Segment,
    modifier: Modifier = Modifier,
    onScrollAlpha: (Float, Boolean) -> Unit = { _, _ -> },
) {
    val listState = rememberLazyListState()
    val scrollAlpha = rememberScrollAlpha(listState = listState)
    val collapsed by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
    ) {
        item("cover") {
            SegmentCover(cover = segment.cover)
        }

        item {
            Spacer(
                Modifier
                    .fillMaxWidth()
                    .height(12.dp)
            )
        }

        item {
            Text(
                text = segment.subtitle ?: "",
                modifier = Modifier,
                style = SsTheme.typography.titleMedium
            )
        }

        item {
            Spacer(
                Modifier
                    .fillMaxWidth()
                    .height(12.dp)
            )
        }

        items(segment.blocks.orEmpty(), key = { it.id }) { item ->
            Text(
                text = "Block with ID: ${item.id}",
                modifier = Modifier,
                style = SsTheme.typography.titleMedium
            )

            Spacer(
                Modifier
                    .fillMaxWidth()
                    .height(12.dp)
            )
        }

        items(segment.blocks.orEmpty(), key = { "${it.id} - rand" }) { item ->
            Text(
                text = "Block with ID: ${item.id}",
                modifier = Modifier,
                style = SsTheme.typography.titleMedium
            )

            Spacer(
                Modifier
                    .fillMaxWidth()
                    .height(12.dp)
            )
        }
    }

    LaunchedEffect(scrollAlpha) {
        snapshotFlow { scrollAlpha.alpha }.collect { alpha ->
            onScrollAlpha(alpha, collapsed)
        }
    }
}
