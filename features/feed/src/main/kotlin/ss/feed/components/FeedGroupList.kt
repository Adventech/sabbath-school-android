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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.ss.design.compose.theme.Dimens
import io.adventech.blockkit.model.feed.FeedDirection
import io.adventech.blockkit.model.feed.FeedGroup
import io.adventech.blockkit.model.resource.Resource
import kotlinx.collections.immutable.ImmutableList
import ss.feed.components.view.FeedGroupView
import ss.feed.components.view.FeedResourceView
import ss.feed.model.FeedResourceSpec
import ss.feed.model.toSpec

@Composable
internal fun FeedLazyColum(
    groups: ImmutableList<FeedGroup>,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(),
    seeAllClick: (FeedGroup) -> Unit = {},
    itemClick: (Resource) -> Unit = {},
) {
    LazyColumn(
        modifier = modifier.navigationBarsPadding(),
        state = state,
        contentPadding = contentPadding,
    ) {
        if (groups.size == 1) {
            val group = groups.first()

            checkDirection(group = group, seeAllClick = { seeAllClick(group) }, itemClick = itemClick)

        } else {
            items(groups, key = { it.id }) { group ->
                when (group.direction) {
                    FeedDirection.UNKNOWN -> Unit
                    FeedDirection.VERTICAL -> {
                        group.resources?.forEach { resource ->
                            FeedResourceView(resource.toSpec(group), Modifier.padding(8.dp)) {
                                itemClick(resource)
                            }
                        }
                    }

                    FeedDirection.HORIZONTAL -> {
                        FeedGroupView(group = group, seeAllClick = { seeAllClick(group) }, itemClick = itemClick)
                    }
                }
            }
        }

        item("insets") {
            Spacer(
                Modifier
                    .fillMaxWidth()
                    .windowInsetsBottomHeight(WindowInsets.navigationBars)
            )
        }

        item("spacer") {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            ) // calculate bottom nav height
        }

    }
}

private fun LazyListScope.checkDirection(
    group: FeedGroup,
    seeAllClick: () -> Unit,
    itemClick: (Resource) -> Unit
) {
    when (group.direction) {
        FeedDirection.UNKNOWN -> Unit
        FeedDirection.VERTICAL -> {
            items(group.resources.orEmpty(), key = { it.id }) { resource ->
                FeedResourceView(resource.toSpec(group), Modifier.padding(8.dp)) {
                    itemClick(resource)
                }
            }
        }

        FeedDirection.HORIZONTAL -> {
            item(key = group.id) {
                FeedGroupView(group = group, seeAllClick = seeAllClick, itemClick = itemClick)
            }
        }
    }
}

@Composable
internal fun FeedLazyColum(
    resources: ImmutableList<FeedResourceSpec>,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(),
    itemClick: (String) -> Unit = {},
) {
    LazyColumn(
        modifier = modifier,
        state = state,
        contentPadding = contentPadding,
    ) {
        items(resources, key = { it.id }) { spec ->
            FeedResourceView(
                spec = spec,
                modifier = Modifier.padding(horizontal = Dimens.grid_4, vertical = Dimens.grid_3)
            ) {
                itemClick(spec.index)
            }
        }
    }
}

