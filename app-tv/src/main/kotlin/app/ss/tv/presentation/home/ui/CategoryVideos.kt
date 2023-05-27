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

package app.ss.tv.presentation.home.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.foundation.PivotOffsets
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.itemsIndexed
import androidx.tv.material3.ImmersiveList
import androidx.tv.material3.ImmersiveListScope
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import app.ss.tv.data.model.CategorySpec
import app.ss.tv.data.model.VideoSpec
import app.ss.tv.presentation.theme.Padding
import app.ss.tv.presentation.theme.rememberChildPadding
import app.ss.tv.presentation.utils.FocusGroup
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun CategoryVideos(
    category: CategorySpec,
    modifier: Modifier = Modifier,
    onVideoClick: (VideoSpec) -> Unit = {},
) {
    var currentItemIndex by remember { mutableStateOf(0) }
    var isListFocused by remember { mutableStateOf(false) }
    var listCenterOffset by remember { mutableStateOf(Offset.Zero) }
    val listHeight by animateDpAsState(
        if (isListFocused) LocalConfiguration.current.screenHeightDp.times(0.6f).dp else 260.dp
    )

    ImmersiveList(
        modifier = modifier
            .height(listHeight)
            .fillMaxWidth(),
        background = { _, listHasFocus ->
            isListFocused = listHasFocus
            val gradientColor = MaterialTheme.colorScheme.surface

            AnimatedVisibility(
                visible = isListFocused,
                exit = fadeOut() + shrinkVertically(),
                modifier = Modifier
                    .height(LocalConfiguration.current.screenHeightDp.times(0.8f).dp)
                    .drawWithCache {
                        onDrawWithContent {
                            if (listCenterOffset == Offset.Zero) {
                                listCenterOffset = center
                            }
                            drawContent()
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        gradientColor,
                                        Color.Transparent
                                    ),
                                    startX = size.width.times(0.2f),
                                    endX = size.width.times(0.7f)
                                )
                            )
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        gradientColor
                                    ),
                                    endY = size.width.times(0.3f)
                                )
                            )
                            drawRect(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        gradientColor,
                                        Color.Transparent
                                    ),
                                    start = Offset(
                                        size.width.times(0.2f),
                                        size.height.times(0.5f)
                                    ),
                                    end = Offset(
                                        size.width.times(0.9f),
                                        0f
                                    )
                                )
                            )
                        }
                    }
            ) {
                val video = remember(category.videos, currentItemIndex) {
                    category.videos[currentItemIndex]
                }
                AsyncImage(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(LocalConfiguration.current.screenHeightDp.times(0.8f).dp),
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(video.thumbnail)
                        .build(),
                    contentDescription = video.title,
                    contentScale = ContentScale.Crop
                )
            }
        },
        list = {
            Column {
                AnimatedVisibility(visible = isListFocused) {
                    val video = remember(category.videos, currentItemIndex) {
                        category.videos[currentItemIndex]
                    }
                    Column(
                        modifier = Modifier.padding(
                            start = rememberChildPadding().start,
                            bottom = 32.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = video.title,
                            style = MaterialTheme.typography.displaySmall
                        )

                        Text(
                            modifier = Modifier.fillMaxWidth(0.5f),
                            text = video.artist,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                            fontWeight = FontWeight.Light
                        )
                    }
                }
                ImmersiveListVideosRow(
                    videos = category.videos,
                    title = if (isListFocused) null else category.title,
                    showItemTitle = !isListFocused,
                    onVideoClick = onVideoClick,
                    focusedItemIndex = { focusedIndex ->
                        currentItemIndex = focusedIndex
                    }
                )
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImmersiveListScope.ImmersiveListVideosRow(
    videos: List<VideoSpec>,
    modifier: Modifier = Modifier,
    childPadding: Padding = rememberChildPadding(),
    title: String? = null,
    titleStyle: TextStyle = MaterialTheme.typography.headlineLarge.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 30.sp
    ),
    showItemTitle: Boolean = true,
    focusedItemIndex: (Int) -> Unit = {},
    onVideoClick: (VideoSpec) -> Unit = {}
) {
    Column(
        modifier = modifier.focusGroup()
    ) {
        title?.let { nnTitle ->
            Text(
                text = nnTitle,
                style = titleStyle,
                modifier = Modifier
                    .alpha(1f)
                    .padding(start = childPadding.start)
                    .padding(vertical = 16.dp)
            )
        }

        AnimatedContent(
            targetState = videos,
            label = "",
        ) { videosState ->
            FocusGroup {
                TvLazyRow(
                    pivotOffsets = PivotOffsets(parentFraction = 0.07f),
                    contentPadding = PaddingValues(start = childPadding.start, end = childPadding.end)
                ) {
                    itemsIndexed(videosState, key = { _, model -> model.id }) {index, video ->
                        VideoRowItem(
                            index = index,
                            video = video,
                            showItemTitle = showItemTitle,
                            focusedItemIndex = focusedItemIndex,
                            onVideoClick = onVideoClick,
                            modifier = Modifier
                                .immersiveListItem(index)
                                .restorableFocus()
                                .padding(end = 20.dp),
                        )
                    }
                }
            }
        }
    }
}
