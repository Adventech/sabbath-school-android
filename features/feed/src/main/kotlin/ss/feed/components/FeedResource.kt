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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ss.design.compose.theme.Dimens
import app.ss.models.feed.FeedDirection
import app.ss.models.feed.FeedView
import ss.feed.model.FeedResourceSpec

@Composable
internal fun FeedResource(
    spec: FeedResourceSpec,
    modifier: Modifier = Modifier,
) {
    val coverSpec by remember(spec) {
        mutableStateOf(
            FeedResourceCoverSpec(
                title = spec.title,
                direction = spec.direction,
                covers = spec.covers,
                view = spec.view,
                primaryColor = spec.primaryColor
            )
        )
    }

    when (spec.view) {
        FeedView.UNKNOWN,
        FeedView.tile,
        FeedView.banner,
        FeedView.square -> {
            when (spec.direction) {
                FeedDirection.UNKNOWN -> Unit
                FeedDirection.vertical -> VerticalFolio(spec, coverSpec, modifier)
                FeedDirection.horizontal -> HorizontalFolio(spec, coverSpec, modifier)
            }
        }

        FeedView.folio -> {
            when (spec.direction) {
                FeedDirection.UNKNOWN -> Unit
                FeedDirection.vertical -> VerticalFolio(spec, coverSpec, modifier)
                FeedDirection.horizontal -> HorizontalFolio(spec, coverSpec, modifier)
            }
        }
    }
}

@Composable
private fun VerticalFolio(spec: FeedResourceSpec, coverSpec: FeedResourceCoverSpec, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .padding(
                vertical = Dimens.grid_2,
                horizontal = Dimens.grid_4
            )
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FeedResourceCover(coverSpec)

        Spacer(modifier = Modifier.width(Dimens.grid_4))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp)
        ) {
            Text(
                text = spec.date.uppercase(),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 13.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
            )

            Spacer(modifier = Modifier.height(Dimens.grid_1))

            Text(
                text = spec.title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 24.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
            )
        }
    }
}

@Composable
private fun HorizontalFolio(spec: FeedResourceSpec, coverSpec: FeedResourceCoverSpec, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .width(IntrinsicSize.Min)
            .padding(bottom = Dimens.grid_1)
    ) {
        FeedResourceCover(coverSpec)

        Spacer(modifier = Modifier.height(Dimens.grid_2))

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
                text = spec.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 15.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private val TitleMinHeight = 40.dp
