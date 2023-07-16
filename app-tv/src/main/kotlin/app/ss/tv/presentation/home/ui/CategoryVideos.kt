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

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.CacheDrawScope
import androidx.compose.ui.draw.DrawResult
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
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
import app.ss.tv.presentation.theme.SSTvTheme
import app.ss.tv.presentation.theme.rememberChildPadding
import app.ss.tv.presentation.utils.FocusGroup
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.collections.immutable.ImmutableList

@Composable
fun CategoryVideos(
    category: CategorySpec,
    modifier: Modifier = Modifier,
    onVideoClick: (VideoSpec) -> Unit = {},
) {
    var currentItemIndex by remember { mutableIntStateOf(0) }
    var isListFocused by remember { mutableStateOf(false) }
    val listHeight by animateDpAsState(if (isListFocused) ImmersiveBgHeight else 280.dp)

    ImmersiveList(
        background = { index, listHasFocus ->
            currentItemIndex = index
            isListFocused = listHasFocus

            AnimatedVisibility(
                visible = isListFocused,
                modifier = Modifier.height(ImmersiveBgHeight),
            ) {
                ImmersiveListBackground(video = category.videos[index])
            }
        },
        modifier = modifier
            .height(listHeight)
            .fillMaxWidth(),
        listAlignment = Alignment.BottomStart,
    ) {
        ImmersiveListVideosRow(
            videos = category.videos,
            title = category.title.takeUnless { isListFocused },
            onVideoClick = onVideoClick,
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ImmersiveListScope.ImmersiveListVideosRow(
    videos: ImmutableList<VideoSpec>,
    title: String?,
    onVideoClick: (VideoSpec) -> Unit,
    modifier: Modifier = Modifier,
    childPadding: Padding = rememberChildPadding()
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        title?.let {
            Text(
                text = it,
                modifier = Modifier
                    .padding(start = childPadding.start)
                    .padding(vertical = childPadding.top),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Medium,
                ),
            )
        }

        TvLazyRow(
            modifier = modifier,
            pivotOffsets = PivotOffsets(parentFraction = 0.07f),
            contentPadding = PaddingValues(start = childPadding.start, end = childPadding.end),
            verticalAlignment = Alignment.CenterVertically
        ) {
            itemsIndexed(videos, key = { _, model -> model.id }) { index, video ->
                var isItemFocused by remember { mutableStateOf(false) }
                val scale by animateFloatAsState(if (isItemFocused) 1.0f else 0.9f)

                VideoRowItem(
                    video = video,
                    onVideoClick = onVideoClick,
                    modifier = Modifier
                        .immersiveListItem(index)
                        .focusable()
                        .scale(scale)
                        .onFocusChanged { isItemFocused = it.isFocused }
                        .focusProperties {
                            if (index == 0) {
                                left = FocusRequester.Cancel
                            }
                        },
                )

                Spacer(modifier = Modifier.width(16.dp))
            }
        }
    }
}

@Composable
private fun ImmersiveListBackground(
    video: VideoSpec,
    modifier: Modifier = Modifier,
    gradientColor: Color = MaterialTheme.colorScheme.surface
) {
    Box(modifier = modifier.fillMaxSize()) {
        AsyncImage(
            modifier = Modifier
                .aspectRatio(ASPECT_RATIO)
                .drawWithCache { drawImmersiveListBackground(gradientColor) }
                .align(Alignment.TopEnd),
            model = ImageRequest.Builder(LocalContext.current)
                .data(video.thumbnail)
                .build(),
            contentDescription = video.title,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = rememberChildPadding().start),
            verticalArrangement = Arrangement.Bottom,
        ) {
            Text(
                text = video.title,
                style = MaterialTheme.typography.displaySmall
            )

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
            )

            Text(
                text = video.artist,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                fontWeight = FontWeight.Light
            )

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(LocalConfiguration.current.screenHeightDp.times(0.3f).dp)
            )
        }
    }
}

private fun CacheDrawScope.drawImmersiveListBackground(
    gradientColor: Color
): DrawResult = onDrawWithContent {
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
            endY = size.width.times(0.5f)
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

private val ImmersiveBgHeight = 426.dp

@Preview(
    name = "Immersive List Bg",
    device = Devices.TV_1080p
)
@Composable
private fun PreviewListBackground() {
    SSTvTheme {
        ImmersiveListBackground(
            video = VideoSpec(
                id = "id",
                title = "Paul and the Ephesians",
                artist = "Hope Sabbath School",
                src = "",
                thumbnail = "image.png"
            ),
        )
    }
}
