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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ss.lessons.data.model.SSQuarterly
import coil.compose.rememberImagePainter
import coil.transform.RoundedCornersTransformation
import com.cryart.design.theme.BaseGrey2
import com.cryart.design.theme.BodyMedium1
import com.cryart.design.theme.LabelSmall
import com.cryart.design.theme.OffWhite
import com.cryart.design.theme.Spacing16
import com.cryart.design.theme.Spacing20
import com.cryart.design.theme.Spacing4
import com.cryart.design.theme.Spacing8
import com.cryart.design.theme.Title
import com.cryart.design.theme.TitleSmall
import com.cryart.design.theme.iconTint
import com.cryart.design.theme.navTitle
import com.cryart.design.theme.parse

fun SSQuarterly.spec(
    type: QuarterlySpec.Type,
    onClick: () -> Unit = {}
): QuarterlySpec = QuarterlySpec(
    title,
    human_date,
    cover,
    Color.parse(color_primary),
    type,
    onClick
)

data class QuarterlySpec(
    val title: String,
    val date: String,
    val cover: String,
    val color: Color,
    val type: Type = Type.NORMAL,
    val onClick: () -> Unit = {}
) {
    enum class Type(
        val width: Dp,
        val height: Dp
    ) {
        NORMAL(130.dp, 165.dp),
        LARGE(170.dp, 268.dp);
    }
}

@Composable
private fun CoverBox(
    modifier: Modifier = Modifier,
    type: QuarterlySpec.Type,
    color: Color,
    content: @Composable () -> Unit
) {
    val paddingVertical: Dp
    val paddingHorizontal: Dp
    when (type) {
        QuarterlySpec.Type.NORMAL -> {
            paddingVertical = Spacing8
            paddingHorizontal = Spacing16
        }
        QuarterlySpec.Type.LARGE -> {
            paddingVertical = Spacing16
            paddingHorizontal = Spacing8
        }
    }

    Box(
        modifier = modifier
            .size(
                type.width,
                type.height
            )
            .padding(
                horizontal = paddingHorizontal,
                vertical = paddingVertical
            )
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
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {

        QuarterlyCover(spec)

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
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = spec.title,
                style = Title.copy(
                    color = MaterialTheme.colors.onSurface
                ),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
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
            .padding(bottom = Spacing4)
    ) {
        QuarterlyCover(spec)

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .sizeIn(minHeight = TitleMinHeight)
                .padding(horizontal = Spacing16),
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
            QuarterlySpec.Type.LARGE
        ),
        modifier = Modifier.padding(6.dp)
    )
}

@Composable
private fun QuarterlyCover(spec: QuarterlySpec) {
    CoverBox(
        type = spec.type,
        color = spec.color
    ) {
        Image(
            painter = rememberImagePainter(
                data = spec.cover,
                builder = {
                    crossfade(true)
                    transformations(RoundedCornersTransformation(20f))
                }
            ),
            contentDescription = spec.title,
            contentScale = ContentScale.FillBounds
        )
    }
}

@Composable
fun GroupedQuarterliesColumn(
    title: String,
    items: List<QuarterlySpec>,
    lastIndex: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .groupBackground(
                darkTheme = isSystemInDarkTheme(),
                lastIndex = lastIndex
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                    horizontal = Spacing20
                )
            )

            TextButton(
                onClick = { },
            ) {
                Text(
                    text = "See All",
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
        Spacer(modifier = Modifier.height(Spacing8))

        LazyRow(
            contentPadding = PaddingValues(
                horizontal = Spacing8,
                vertical = Spacing4
            )
        ) {
            items(items) { item ->
                QuarterlyColumn(
                    spec = item,
                    modifier = Modifier
                        .clickable(onClick = item.onClick),
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing16))
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
                    OffWhite.copy(alpha = 0.3f)
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
