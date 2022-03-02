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

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ss.models.SSQuarterly
import coil.compose.rememberImagePainter
import coil.transform.RoundedCornersTransformation
import com.cryart.design.theme.BaseGrey2
import com.cryart.design.theme.BodyMedium1
import com.cryart.design.theme.Dimens
import com.cryart.design.theme.LabelSmall
import com.cryart.design.theme.OffWhite
import com.cryart.design.theme.Spacing16
import com.cryart.design.theme.Spacing8
import com.cryart.design.theme.Title
import com.cryart.design.theme.TitleSmall
import com.cryart.design.theme.iconTint
import com.cryart.design.theme.isLargeScreen
import com.cryart.design.theme.navTitle
import com.cryart.design.theme.parse
import com.cryart.sabbathschool.lessons.R
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import dev.chrisbanes.snapper.ExperimentalSnapperApi
import dev.chrisbanes.snapper.SnapOffsets
import dev.chrisbanes.snapper.rememberSnapperFlingBehavior

fun SSQuarterly.spec(
    type: QuarterlySpec.Type,
    onClick: () -> Unit = {}
): QuarterlySpec = QuarterlySpec(
    title,
    human_date,
    cover,
    Color.parse(color_primary),
    isPlaceholder = isPlaceholder,
    type,
    onClick
)

data class QuarterlySpec(
    val title: String,
    val date: String,
    val cover: String,
    val color: Color,
    val isPlaceholder: Boolean = false,
    val type: Type = Type.NORMAL,
    val onClick: () -> Unit = {}
) {
    enum class Type {
        NORMAL,
        LARGE;

        fun width(largeScreen: Boolean): Dp = when {
            largeScreen && this == NORMAL -> 150.dp
            largeScreen && this == LARGE -> 198.dp
            this == NORMAL -> 98.dp
            this == LARGE -> 148.dp
            else -> Dp.Unspecified
        }

        fun height(largeScreen: Boolean): Dp = when {
            largeScreen && this == NORMAL -> 198.dp
            largeScreen && this == LARGE -> 276.dp
            this == NORMAL -> 146.dp
            this == LARGE -> 226.dp
            else -> Dp.Unspecified
        }
    }
}

@Composable
private fun CoverBox(
    modifier: Modifier = Modifier,
    type: QuarterlySpec.Type,
    color: Color,
    content: @Composable () -> Unit
) {
    val isLargeScreen = isLargeScreen()
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
fun QuarterlyRow(
    spec: QuarterlySpec,
    modifier: Modifier = Modifier
) {
    val loadingModifier = Modifier.placeholder(
        visible = spec.isPlaceholder,
        highlight = PlaceholderHighlight.shimmer(),
    )

    Row(
        modifier = modifier
            .padding(
                vertical = Dimens.grid_2,
                horizontal = Dimens.grid_4
            )
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
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
                style = BodyMedium1.copy(
                    fontWeight = FontWeight.Bold,
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .then(loadingModifier)
            )

            Spacer(modifier = Modifier.height(Dimens.grid_1))

            Text(
                text = spec.title,
                style = Title.copy(
                    color = MaterialTheme.colors.onSurface
                ),
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
            "Rest In Christ",
            "July · August · September 2021",
            "https://sabbath-school.adventech.io/api/v1/en/quarterlies/2021-03/cover.png",
            Color.Cyan,
            false,
            QuarterlySpec.Type.NORMAL
        ),
        modifier = Modifier.padding(6.dp)
    )
}

// begin column
@Composable
fun QuarterlyColumn(
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
            style = TitleSmall.copy(
                color = navTitle(),
                lineHeight = 18.sp
            ),
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
            "Rest In Christ",
            "July · August · September 2021",
            "https://sabbath-school.adventech.io/api/v1/en/quarterlies/2021-03/cover.png",
            Color.Magenta,
            false,
            QuarterlySpec.Type.LARGE
        ),
        modifier = Modifier.padding(6.dp)
    )
}

@Composable
private fun QuarterlyCover(
    spec: QuarterlySpec,
    modifier: Modifier = Modifier
) {
    CoverBox(
        type = spec.type,
        color = spec.color,
        modifier = modifier
    ) {
        Image(
            painter = rememberImagePainter(
                data = spec.cover,
                builder = {
                    crossfade(true)
                    transformations(RoundedCornersTransformation(18f))
                }
            ),
            contentDescription = spec.title,
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
fun GroupedQuarterliesColumn(
    title: String,
    items: List<QuarterlySpec>,
    lastIndex: Boolean,
    modifier: Modifier = Modifier,
    seeAllClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .groupBackground(
                darkTheme = isSystemInDarkTheme(),
                lastIndex = lastIndex
            )
    ) {
        GroupTitle(title, seeAllClick)

        Spacer(modifier = Modifier.height(Dimens.grid_2))

        GroupQuarterlies(items)

        Spacer(modifier = Modifier.height(Dimens.grid_4))
    }
}

@OptIn(ExperimentalSnapperApi::class)
@Composable
private fun GroupQuarterlies(
    items: List<QuarterlySpec>,
    modifier: Modifier = Modifier,
) {
    val lazyListState = rememberLazyListState()
    val contentPadding = PaddingValues(
        horizontal = Dimens.grid_4,
    )

    LazyRow(
        state = lazyListState,
        flingBehavior = rememberSnapperFlingBehavior(
            lazyListState = lazyListState,
            snapOffsetForItem = SnapOffsets.Start,
            endContentPadding = contentPadding.calculateEndPadding(LayoutDirection.Ltr),
        ),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(Dimens.grid_4),
        modifier = modifier
    ) {
        itemsIndexed(items, key = { _: Int, spec: QuarterlySpec -> spec.title }) { _, item ->
            QuarterlyColumn(
                spec = item,
                modifier = Modifier
                    .clickable(onClick = item.onClick),
            )
        }
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
            style = Title.copy(
                color = BaseGrey2,
                fontSize = 13.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(
                start = Dimens.grid_5,
                end = Dimens.grid_2
            )
        )

        TextButton(
            onClick = seeAllClick,
        ) {
            Text(
                text = stringResource(id = R.string.ss_see_all),
                style = LabelSmall.copy(
                    color = MaterialTheme.colors.primary,
                ),
            )

            Icon(
                Icons.Rounded.KeyboardArrowRight,
                contentDescription = "Arrow Right",
                tint = iconTint()
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
                    OffWhite.copy(alpha = 0.2f)
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
        "Grouping",
        emptyList(),
        true,
        Modifier
            .padding(4.dp)
            .fillMaxWidth()
    )
}
