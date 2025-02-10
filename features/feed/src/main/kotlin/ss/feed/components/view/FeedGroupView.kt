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

package ss.feed.components.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ss.design.compose.theme.Dimens
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.widget.icon.IconBox
import app.ss.design.compose.widget.icon.Icons
import app.ss.design.compose.widget.list.SnappingLazyRow
import io.adventech.blockkit.model.feed.FeedDirection
import io.adventech.blockkit.model.feed.FeedGroup
import io.adventech.blockkit.model.feed.FeedScope
import io.adventech.blockkit.model.feed.FeedType
import io.adventech.blockkit.model.feed.FeedView
import io.adventech.blockkit.model.resource.Resource
import ss.feed.components.PlaceHolders
import ss.feed.model.toSpec

@Composable
internal fun FeedGroupView(
    group: FeedGroup,
    modifier: Modifier = Modifier,
    seeAllClick: () -> Unit = {},
    itemClick: (Resource) -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = Dimens.grid_4),
        verticalArrangement = Arrangement.spacedBy(Dimens.grid_2),
    ) {
        FeedGroupHeader(group.title, group.seeAll, Modifier, seeAllClick)

        SnappingLazyRow(modifier = Modifier.fillMaxWidth()) {
            items(group.resources.orEmpty(), key = { it.id }) { resource ->
                FeedResourceView(resource.toSpec(group), Modifier.padding(horizontal = Dimens.grid_4)) {
                    itemClick(resource)
                }
            }
        }
    }
}

@Composable
private fun FeedGroupHeader(
    title: String?,
    seeAll: String?,
    modifier: Modifier = Modifier,
    seeAllClick: () -> Unit = {},
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title?.uppercase() ?: "",
            modifier = Modifier
                .weight(1f)
                .padding(
                    start = Dimens.grid_4,
                    end = Dimens.grid_2
                ),
            style = SsTheme.typography.titleSmall.copy(
                fontSize = 13.sp,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        seeAll?.let {
            TextButton(
                onClick = seeAllClick
            ) {
                Text(
                    text = it,
                    style = SsTheme.typography.bodySmall.copy(
                        fontSize = 15.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )

                IconBox(
                    icon = Icons.ArrowRight,
                    contentColor = SsTheme.colors.icons
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun FeedGroupViewPreview() {
    SsTheme {
        Surface {
            FeedGroupView(
                group = FeedGroup(
                    id = "id",
                    title = "Title",
                    direction = FeedDirection.HORIZONTAL,
                    resources = listOf(
                        PlaceHolders.FEED_RESOURCE,
                        PlaceHolders.FEED_RESOURCE.copy(
                            id = "id2",
                            primaryColor = "#E8E9E0"
                        ),
                        PlaceHolders.FEED_RESOURCE.copy(
                            id = "id3",
                        ),
                    ),
                    type = FeedType.AIJ,
                    scope = FeedScope.DOCUMENT,
                    view = FeedView.FOLIO,
                    seeAll = "See All",
                )
            )
        }
    }
}
