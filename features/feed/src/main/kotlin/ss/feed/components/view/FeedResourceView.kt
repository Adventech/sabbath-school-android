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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ss.design.compose.theme.Dimens
import app.ss.design.compose.theme.SsTheme
import io.adventech.blockkit.model.feed.FeedView
import ss.feed.components.FeedResourceCover
import ss.feed.components.PlaceHolders
import ss.feed.model.FeedResourceSpec
import ss.feed.model.FeedResourceSpec.ContentDirection
import ss.feed.model.toSpec

/** Composable for a [FeedView]. */
@Composable
internal fun FeedResourceView(
    spec: FeedResourceSpec,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    FeedResourceContainer(
        direction = spec.direction,
        cover = { FeedResourceCover(spec.coverSpec) },
        content = { FeedResourceTitle(spec.title, spec.subtitle, spec.direction) },
        modifier = modifier,
        onClick = onClick,
    )
}

@Composable
private fun FeedResourceContainer(
    direction: ContentDirection,
    cover: @Composable () -> Unit,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    when (direction) {
        ContentDirection.VERTICAL -> {
            Column(
                modifier = modifier
                    .width(IntrinsicSize.Min)
                    .padding(bottom = Dimens.grid_1)
                    .clickable(enabled = onClick != null) { onClick?.invoke() },
                verticalArrangement = Arrangement.spacedBy(Dimens.grid_2)
            ) {
                cover()
                content()
            }
        }

        ContentDirection.HORIZONTAL -> {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(bottom = Dimens.grid_1)
                    .clickable(enabled = onClick != null) { onClick?.invoke() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.grid_2),
            ) {
                cover()
                content()
            }
        }
    }
}

@Composable
private fun FeedResourceTitle(
    title: String,
    subtitle: String?,
    direction: ContentDirection,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.grid_1),
    ) {
        Text(
            text = title,
            modifier = Modifier,
            style = SsTheme.typography.titleSmall.copy(
                fontSize = if (direction == ContentDirection.HORIZONTAL) 20.sp else 15.sp
            ),
            color = SsTheme.colors.primaryForeground,
            lineHeight = 18.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        if (direction == ContentDirection.HORIZONTAL) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
            )

            subtitle?.let {
                Text(
                    text = it,
                    modifier = Modifier,
                    style = SsTheme.typography.bodySmall.copy(
                        fontSize = 14.sp
                    ),
                    color = SsTheme.colors.secondaryForeground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}


// Begin Preview

@PreviewLightDark
@Composable
private fun VerticalPreview() {
    SsTheme {
        Surface {
            FeedResourceView(
                spec = PlaceHolders.FEED_RESOURCE.toSpec(PlaceHolders.FEED_GROUP),
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun HorizontalPreview() {
    SsTheme {
        Surface {
            FeedResourceView(
                spec = PlaceHolders.FEED_RESOURCE.toSpec(PlaceHolders.FEED_GROUP, ContentDirection.HORIZONTAL),
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

// End Preview

