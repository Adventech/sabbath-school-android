/*
 * Copyright (c) 2021. Adventech <info@adventech.io>
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
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.cryart.sabbathschool.lessons.ui.quarterlies.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ss.design.compose.extensions.isLargeScreen
import app.ss.design.compose.extensions.modifier.asPlaceholder
import app.ss.design.compose.theme.Dimens
import app.ss.design.compose.theme.Spacing16
import app.ss.design.compose.theme.Spacing8
import app.ss.design.compose.theme.SsColor
import app.ss.design.compose.theme.iconTint
import app.ss.design.compose.widget.content.ContentBox
import app.ss.design.compose.widget.icon.IconBox
import app.ss.design.compose.widget.icon.Icons
import app.ss.design.compose.widget.image.RemoteImage
import app.ss.design.compose.widget.list.SnappingLazyRow
import com.cryart.sabbathschool.lessons.R
import com.cryart.sabbathschool.lessons.ui.quarterlies.model.GroupedQuarterliesSpec
import com.cryart.sabbathschool.lessons.ui.quarterlies.model.QuarterlySpec

@Composable
private fun CoverBox(
    modifier: Modifier = Modifier,
    type: QuarterlySpec.Type,
    color: Color,
    isLargeScreen: Boolean = isLargeScreen(),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(
                type.width(isLargeScreen),
                type.height(isLargeScreen)
            )
            .padding(Dimens.grid_1)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize(),
            color = color,
            elevation = Spacing8,
            shape = RoundedCornerShape(Spacing8),
            content = content
        )
    }
}

@Composable
internal fun QuarterlyRow(
    spec: QuarterlySpec,
    modifier: Modifier = Modifier
) {
    val loadingModifier = Modifier.asPlaceholder(
        visible = spec.isPlaceholder
    )

    Row(
        modifier = modifier
            .padding(
                vertical = Dimens.grid_2,
                horizontal = Dimens.grid_4
            )
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        QuarterlyCover(
            spec,
            modifier = Modifier
                .then(loadingModifier)
        )

        Spacer(modifier = Modifier.width(Dimens.grid_4))

        Column(
            modifier = Modifier.padding(
                end = Spacing16
            )
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
                    .then(loadingModifier)
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
                    .then(loadingModifier)
            )
        }

        Spacer(modifier = Modifier.fillMaxWidth())
    }
}

@Preview(
    name = "Quarterly",
    showBackground = true,
    backgroundColor = 0xFFFFFF
)
@Composable
fun QuarterlyRowPreview() {
    QuarterlyRow(
        QuarterlySpec(
            id = "id",
            title = "Rest In Christ",
            date = "July · August · September 2021",
            cover = "https://sabbath-school.adventech.io/api/v1/en/quarterlies/2021-03/cover.png",
            color = Color.Cyan,
            index = "index",
            isPlaceholder = false,
            QuarterlySpec.Type.NORMAL
        ),
        modifier = Modifier.padding(6.dp)
    )
}

// begin column
@Composable
internal fun QuarterlyColumn(
    spec: QuarterlySpec,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(IntrinsicSize.Min)
            .padding(bottom = Dimens.grid_1)
    ) {
        QuarterlyCover(spec)

        Spacer(modifier = Modifier.height(Dimens.grid_2))

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .sizeIn(minHeight = TitleMinHeight)
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

private val TitleMinHeight = 40.dp

@Preview(
    name = "Quarterly Column",
    showBackground = true,
    backgroundColor = 0xFFFFFF
)
@Composable
fun QuarterlyColumnPreview() {
    QuarterlyColumn(
        QuarterlySpec(
            id = "id",
            title = "Rest In Christ",
            date = "July · August · September 2021",
            cover = "https://sabbath-school.adventech.io/api/v1/en/quarterlies/2021-03/cover.png",
            color = Color.Magenta,
            index = "index",
            isPlaceholder = false,
            type = QuarterlySpec.Type.LARGE
        ),
        modifier = Modifier.padding(6.dp)
    )
}

@Composable
private fun QuarterlyCover(
    spec: QuarterlySpec,
    modifier: Modifier = Modifier
) {
    val image = remember(spec.cover) {
        RemoteImage(
            data = spec.cover,
            contentDescription = spec.title,
            contentScale = ContentScale.Crop,
            loading = {
                Spacer(
                    modifier = Modifier
                        .fillMaxSize()
                        .asPlaceholder(
                            visible = true,
                            color = spec.color
                        )
                )
            },
            error = {
                Spacer(
                    modifier = Modifier
                        .background(color = spec.color)
                        .fillMaxSize()
                )
            }
        )
    }
    CoverBox(
        type = spec.type,
        color = spec.color,
        modifier = modifier
    ) {
        ContentBox(
            content = image,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(18f))
        )
    }
}

@Composable
internal fun GroupedQuarterliesColumn(
    spec: GroupedQuarterliesSpec,
    modifier: Modifier = Modifier,
    darkTheme: Boolean = isSystemInDarkTheme(),
    seeAllClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .groupBackground(
                darkTheme = darkTheme,
                lastIndex = spec.lastIndex
            )
    ) {
        GroupTitle(spec.title, seeAllClick)

        Spacer(modifier = Modifier.height(Dimens.grid_2))

        SnappingLazyRow(
            contentPadding = PaddingValues(horizontal = Dimens.grid_4),
            horizontalArrangement = Arrangement.spacedBy(Dimens.grid_4)
        ) {
            itemsIndexed(spec.items, key = { _: Int, spec: QuarterlySpec -> spec.title }) { _, item ->
                QuarterlyColumn(
                    spec = item,
                    modifier = Modifier
                        .clickable(onClick = item.onClick)
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens.grid_4))
    }
}

@Composable
private fun GroupTitle(
    title: String,
    seeAllClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.titleSmall.copy(
                fontSize = 13.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(
                start = Dimens.grid_5,
                end = Dimens.grid_2
            )
        )

        TextButton(
            onClick = seeAllClick
        ) {
            Text(
                text = stringResource(id = R.string.ss_see_all),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 15.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )

            IconBox(
                icon = Icons.ArrowRight,
                contentColor = iconTint()
            )
        }
    }
}

private fun Modifier.groupBackground(
    darkTheme: Boolean,
    lastIndex: Boolean
): Modifier = then(
    if (darkTheme || lastIndex) {
        Modifier
    } else {
        Modifier.background(
            Brush.verticalGradient(
                colors = listOf(
                    Color.White,
                    SsColor.OffWhite.copy(alpha = 0.2f)
                )
            )
        )
    }
)

@Preview(
    name = "Grouped Quarterlies",
    showBackground = true,
    backgroundColor = 0xFFFFFF
)
@Composable
private fun GroupedQuarterliesColumnPreview() {
    GroupedQuarterliesColumn(
        spec = GroupedQuarterliesSpec(
            "Grouping",
            emptyList(),
            true
        ),
        Modifier
            .padding(4.dp)
            .fillMaxWidth()
    )
}
