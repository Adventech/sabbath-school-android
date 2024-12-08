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

package ss.feed.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.ss.design.compose.widget.list.SnappingLazyRow
import app.ss.models.feed.FeedDirection
import app.ss.models.feed.FeedGroup
import kotlinx.collections.immutable.ImmutableList
import ss.feed.model.toSpec

@Composable
internal fun FeedGroupList(
    groups: ImmutableList<FeedGroup>,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(),
) {
    LazyColumn(
        modifier = modifier,
        state = state,
        contentPadding = contentPadding,
    ) {
        if (groups.size == 1) {
            val group = groups.first()

            checkDirection(group)

        } else {
            items(groups, key = { it.id }) { group ->
                when (group.direction) {
                    FeedDirection.UNKNOWN -> Unit
                    FeedDirection.VERTICAL -> {
                        // what happens here?

                        group.resources.forEach { resource ->
                            FeedResource(resource.toSpec(group), Modifier.padding(8.dp))
                        }
                    }

                    FeedDirection.HORIZONTAL -> {
                        ResourcesRow(group)
                    }
                }
            }
        }

        item("spacer") {
            Spacer(modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)) // calculate bottom nav height
        }

    }
}

private fun LazyListScope.checkDirection(group: FeedGroup) {
    when (group.direction) {
        FeedDirection.UNKNOWN -> Unit
        FeedDirection.VERTICAL -> {
            items(group.resources, key = { it.id }) { resource ->
                FeedResource(resource.toSpec(group), Modifier.padding(8.dp))
            }
        }

        FeedDirection.HORIZONTAL -> {
            item(key = group.id) {
                ResourcesRow(group)
            }
        }
    }
}

@Composable
private fun ResourcesRow(group: FeedGroup) {
    SnappingLazyRow(
        modifier = Modifier.fillMaxWidth(),
    ) {
        items(group.resources, key = { it.id }) { resource ->
            FeedResource(resource.toSpec(group), Modifier.padding(8.dp))
        }
    }
}
