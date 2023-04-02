/*
 * Copyright (c) 2023. Adventech <info@adventech.io>
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

package app.ss.design.compose.widget.pager

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.ss.design.compose.extensions.previews.DayNightPreviews
import app.ss.design.compose.theme.SsTheme

@Immutable
data class PagerState(val currentPage: Int, val pageCount: Int)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PagerIndicator(
    state: PagerState,
    modifier: Modifier = Modifier,
    indicatorCount: Int = 5,
    indicatorSize: Dp = 16.dp,
    indicatorShape: Shape = CircleShape,
    space: Dp = 8.dp,
    activeColor: Color = SsTheme.colors.primary,
    inActiveColor: Color = Color.LightGray,
    onClick: ((Int) -> Unit)? = null
) {
    val listState = rememberLazyListState()
    val totalWidth: Dp = indicatorSize * indicatorCount + space * (indicatorCount - 1)
    val widthInPx = LocalDensity.current.run { indicatorSize.toPx() }
    
    val currentItem = state.currentPage
    val itemCount = state.pageCount

    LaunchedEffect(currentItem) {
        val viewportSize = listState.layoutInfo.viewportSize
        listState.animateScrollToItem(
            currentItem,
            (widthInPx / 2 - viewportSize.width / 2).toInt()
        )
    }

    LazyRow(
        modifier = modifier.width(totalWidth),
        state = listState,
        contentPadding = PaddingValues(vertical = space),
        horizontalArrangement = Arrangement.spacedBy(space),
        userScrollEnabled = false
    ) {

        items(itemCount) { index ->

            val isSelected = (index == currentItem)

            // Index of item in center when odd number of indicators are set
            // for 5 indicators this is 2nd indicator place
            val centerItemIndex = indicatorCount / 2

            val right1 =
                (currentItem < centerItemIndex &&
                    index >= indicatorCount - 1)

            val right2 =
                (currentItem >= centerItemIndex &&
                    index >= currentItem + centerItemIndex &&
                    index <= itemCount - centerItemIndex + 1)
            val isRightEdgeItem = right1 || right2

            val isLeftEdgeItem =
                index <= currentItem - centerItemIndex &&
                    currentItem > centerItemIndex &&
                    index < itemCount - indicatorCount + 1

            val scale by animateFloatAsState(
                when {
                    isSelected -> 1f
                    (isLeftEdgeItem || isRightEdgeItem) -> 0.5f
                    else -> 0.8f
                }
            )
            val indicatorColor by animateColorAsState(
                if (isSelected) activeColor else inActiveColor
            )

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .clip(indicatorShape)
                    .size(indicatorSize)
                    .background(indicatorColor, indicatorShape)
                    .then(
                        if (onClick != null) {
                            Modifier
                                .clickable {
                                    onClick.invoke(index)
                                }
                                .semantics {
                                    invisibleToUser()
                                }
                        } else Modifier
                    )
            )
        }
    }
}

@DayNightPreviews
@Composable
private fun Preview() {
    SsTheme {
        Surface {
            PagerIndicator(
                state = PagerState(3, 10),
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
