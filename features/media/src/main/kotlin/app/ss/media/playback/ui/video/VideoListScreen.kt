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

package app.ss.media.playback.ui.video

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.ss.design.compose.extensions.isLargeScreen
import app.ss.design.compose.extensions.modifier.asPlaceholder
import app.ss.design.compose.extensions.modifier.thenIf
import app.ss.design.compose.extensions.scrollbar.drawVerticalScrollbar
import app.ss.design.compose.theme.Dimens
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.theme.color.SsColors
import app.ss.design.compose.widget.DragHandle
import app.ss.design.compose.widget.content.ContentBox
import app.ss.design.compose.widget.image.RemoteImage
import app.ss.design.compose.widget.list.SnappingLazyRow
import app.ss.media.R
import app.ss.media.playback.ui.spec.VideoSpec
import app.ss.media.playback.ui.spec.VideosInfoSpec
import app.ss.media.playback.ui.spec.toSpec
import app.ss.models.media.SSVideo
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
internal fun VideoListScreen(
    viewModel: VideoListViewModel = viewModel(),
    isAtTop: (Boolean) -> Unit = {},
    onVideoClick: (SSVideo) -> Unit
) {
    val videoList by viewModel.videoListFlow.collectAsStateWithLifecycle()

    VideoListScreen(
        videoList = videoList,
        isAtTop = isAtTop,
        onVideoClick = onVideoClick
    )
}

@Composable
internal fun VideoListScreen(
    videoList: VideoListData,
    modifier: Modifier = Modifier,
    onVideoClick: (SSVideo) -> Unit = {},
    isAtTop: (Boolean) -> Unit = {},
    listState: LazyListState = rememberLazyListState()
) {
    LazyColumn(
        modifier = modifier
            .drawVerticalScrollbar(listState),
        contentPadding = PaddingValues(
            horizontal = 0.dp,
            vertical = 16.dp
        ),
        state = listState
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                DragHandle()
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Text(
                text = stringResource(id = R.string.ss_media_video),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 30.sp
                ),
                color = SsTheme.colors.navTitle,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        when (videoList) {
            VideoListData.Empty -> {
                // todo: show empty view?
            }
            is VideoListData.Horizontal -> {
                items(videoList.data) { videosInfo ->
                    VideosInfoList(
                        spec = videosInfo.toSpec(),
                        target = videoList.target,
                        onVideoClick = onVideoClick
                    )
                }
            }
            is VideoListData.Vertical -> {
                item {
                    val video = videoList.featured
                    VideoColumn(
                        video = video.toSpec(),
                        featured = true,
                        vertical = true,
                        onVideoClick = { onVideoClick(video) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }

                items(videoList.clips) { video ->
                    VideoRow(
                        video = video.toSpec(),
                        onVideoClick = { onVideoClick(video) }
                    )

                    Spacer(modifier = Modifier.height(Dimens.grid_4))
                }
            }
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .map { index -> index == 0 }
            .distinctUntilChanged()
            .collect { isAtTop(it) }
    }
}

@Composable
private fun VideosInfoList(
    spec: VideosInfoSpec,
    target: String?,
    modifier: Modifier = Modifier,
    onVideoClick: (SSVideo) -> Unit,
    listState: LazyListState = rememberLazyListState()
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = spec.artist.uppercase(),
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 13.sp
            ),
            color = if (SsTheme.colors.isDark) SsColors.BaseGrey2 else MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(top = 16.dp)
        )

        SnappingLazyRow(
            state = listState,
            contentPadding = PaddingValues(
                horizontal = 16.dp,
                vertical = 16.dp
            )
        ) {
            itemsIndexed(
                spec.clips,
                key = { _: Int, item: SSVideo -> item.id }
            ) { _, video ->
                VideoColumn(
                    video = video.toSpec(),
                    modifier = Modifier,
                    onVideoClick = { onVideoClick(video) }
                )
            }
        }
    }

    // scroll to the most relevant video
    LaunchedEffect(target) {
        val index = spec.clips.indexOfFirst { it.targetIndex == target }
        if (index > 0) {
            listState.scrollToItem(index)
        }
    }
}

@Composable
private fun VideoColumn(
    video: VideoSpec,
    modifier: Modifier = Modifier,
    featured: Boolean = false,
    vertical: Boolean = false,
    onVideoClick: () -> Unit
) {
    val defSize = getThumbnailSize(vertical = vertical)
    val size = if (featured) {
        val screenWidth = screenWidth() - (24 * 2)
        Size(
            width = screenWidth.toFloat(),
            height = (screenWidth / (defSize.width / defSize.height))
        )
    } else {
        defSize
    }

    Column(
        modifier = modifier
            .width(IntrinsicSize.Min)
            .padding(horizontal = 8.dp)
            .thenIf(featured) {
                Modifier.padding(
                    horizontal = 24.dp
                )
            }
            .clickable {
                onVideoClick()
            }
    ) {
        VideoImage(video, size)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = video.title,
            style = MaterialTheme.typography.titleSmall.copy(
                fontSize = if (featured) 19.sp else 16.sp
            ),
            color = SsTheme.colors.navTitle
        )
        Text(
            text = video.artist,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 14.sp
            ),
            color = SsTheme.colors.onSurfaceSecondary
        )
    }
}

@Composable
private fun VideoRow(
    video: VideoSpec,
    modifier: Modifier = Modifier,
    onVideoClick: () -> Unit
) {
    val size = getThumbnailSize(vertical = true)

    Row(
        horizontalArrangement = Arrangement.spacedBy(Dimens.grid_2),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(
                horizontal = 24.dp
            )
            .clickable {
                onVideoClick()
            }
    ) {
        VideoImage(video, size)

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = video.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 16.sp,
                    lineHeight = TextUnit.Unspecified
                ),
                color = SsTheme.colors.navTitle,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = video.artist,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 14.sp
                ),
                color = SsTheme.colors.onSurfaceSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun VideoImage(
    video: VideoSpec,
    size: Size
) {
    val placeholder: @Composable () -> Unit = {
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .asPlaceholder(visible = true)
                .shadow(CoverCornerRadius, CoverImageShape)
        )
    }

    ContentBox(
        content = RemoteImage(
            data = video.thumbnail,
            contentDescription = video.title,
            loading = placeholder,
            error = placeholder
        ),
        modifier = Modifier
            .size(
                width = size.width.dp,
                height = size.height.dp
            )
            .shadow(CoverCornerRadius, CoverImageShape)
            .clip(CoverImageShape)
    )
}

private val CoverCornerRadius = 6.dp
private val CoverImageShape = RoundedCornerShape(CoverCornerRadius)

@Composable
@Stable
private fun getThumbnailSize(vertical: Boolean): Size {
    val ratio = if (vertical) 2.7f else 1.2f
    val largeScreen = isLargeScreen()

    val width = if (largeScreen) defSize.width else screenWidth() / ratio
    val height = if (largeScreen) defSize.height else (width / (defSize.width / defSize.height))

    return Size(
        width = width,
        height = height
    )
}

private val defSize = Size(276f, 149f)

/**
 * On large screens this returns approx width of the BottomSheet and not the actual device width
 */
@Composable
private fun screenWidth(): Int =
    if (isLargeScreen()) 600 else LocalConfiguration.current.screenWidthDp
