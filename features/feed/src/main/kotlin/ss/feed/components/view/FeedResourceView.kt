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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
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
import app.ss.models.feed.FeedDirection
import app.ss.models.feed.FeedView
import app.ss.models.resource.ResourceCovers
import ss.feed.components.FeedResourceCover
import ss.feed.components.FeedResourceCoverSpec
import ss.feed.components.ResourceCoverType

/** Composable for a [FeedView]. */
@Composable
internal fun FeedResourceView(
    title: String,
    coverSpec: FeedResourceCoverSpec,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .width(IntrinsicSize.Min)
            .padding(bottom = Dimens.grid_1)
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        verticalArrangement = Arrangement.spacedBy(Dimens.grid_2)
    ) {
        FeedResourceCover(coverSpec)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .sizeIn(minHeight = TitleMinHeight),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = Dimens.grid_1),
                text = title,
                style = SsTheme.typography.titleSmall.copy(
                    fontSize = 15.sp
                ),
                color = SsTheme.colors.primaryForeground,
                lineHeight = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private val TitleMinHeight = 20.dp

@PreviewLightDark
@Composable
private fun Preview() {
    val coverSpec = FeedResourceCoverSpec(
        title = "The title of the view",
        type = ResourceCoverType.PORTRAIT,
        direction = FeedDirection.VERTICAL,
        covers = ResourceCovers(
            portrait = "https://via.placeholder.com/150",
            landscape = "https://via.placeholder.com/150",
            square = "https://via.placeholder.com/150",
            splash = "https://via.placeholder.com/150"
        ),
        view = FeedView.FOLIO,
        primaryColor = "#94BDFD"
    )

    SsTheme {
        Surface {
            FeedResourceView(title = coverSpec.title, coverSpec, Modifier.padding(8.dp))
        }
    }
}

