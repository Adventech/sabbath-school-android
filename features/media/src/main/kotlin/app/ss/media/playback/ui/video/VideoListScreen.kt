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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.ss.media.R
import app.ss.media.model.SSVideo
import app.ss.media.model.SSVideosInfo
import app.ss.media.playback.ui.common.CoilImage
import app.ss.media.playback.ui.common.rememberFlowWithLifecycle
import app.ss.media.playback.ui.video.player.VideoPlayerActivity
import com.cryart.design.ext.ListSnappingConnection
import com.cryart.design.ext.thenIf
import com.cryart.design.theme.BaseBlue
import com.cryart.design.theme.BaseGrey2
import com.cryart.design.theme.Body
import com.cryart.design.theme.Dimens
import com.cryart.design.theme.Spacing16
import com.cryart.design.theme.Spacing24
import com.cryart.design.theme.Spacing32
import com.cryart.design.theme.Spacing4
import com.cryart.design.theme.Spacing8
import com.cryart.design.theme.Title
import com.cryart.design.theme.TitleSmall
import com.cryart.design.theme.isLargeScreen
import com.cryart.design.theme.navTitle
import com.cryart.design.widgets.DragHandle

import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@OptIn(InternalCoroutinesApi::class)
@Composable
internal fun ViewListScreen(
    viewModel: VideoListViewModel = viewModel(),
    isAtTop: (Boolean) -> Unit = {}
) {

    val videoList by rememberFlowWithLifecycle(viewModel.videoListFlow)
        .collectAsState(VideoListData.Empty)

    val listState = rememberLazyListState()

    val context = LocalContext.current
    val onVideoClick: (SSVideo) -> Unit = { video ->
        val intent = VideoPlayerActivity.launchIntent(
            context,
            video
        )
        context.startActivity(intent)
    }

    LazyColumn(
        contentPadding = PaddingValues(
            horizontal = 0.dp,
            vertical = Spacing16
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
            Spacer(modifier = Modifier.height(Spacing24))
        }

        item {
            Text(
                text = stringResource(id = R.string.ss_media_video),
                style = Title.copy(
                    color = navTitle(),
                    fontSize = 30.sp
                ),
                modifier = Modifier.padding(horizontal = Spacing24)
            )
        }

        item {
            Spacer(modifier = Modifier.height(Spacing16))
        }

        when (videoList) {
            VideoListData.Empty -> {
                // todo: show empty view?
            }
            is VideoListData.Horizontal -> {
                val data = videoList as VideoListData.Horizontal
                items(data.data) { videosInfo ->
                    VideosInfoList(
                        videosInfo = videosInfo,
                        target = data.target,
                        onVideoClick = onVideoClick
                    )
                }
            }
            is VideoListData.Vertical -> {
                val data = videoList as VideoListData.Vertical
                item {
                    VideoColumn(
                        video = data.featured,
                        featured = true,
                        vertical = true,
                        onVideoClick = onVideoClick
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(Spacing32))
                }

                items(data.clips) { video ->
                    VideoRow(
                        video = video,
                        onVideoClick = onVideoClick
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
    videosInfo: SSVideosInfo,
    target: String?,
    modifier: Modifier = Modifier,
    onVideoClick: (SSVideo) -> Unit
) {
    val listState = rememberLazyListState()
    val widthState = remember { mutableStateOf(0) }
    val connection = remember(listState) {
        ListSnappingConnection(
            listState = listState,
            widthState = widthState
        )
    }

    Column(
        modifier = modifier
            .nestedScroll(connection),
    ) {
        Text(
            text = videosInfo.artist.uppercase(),
            style = Title.copy(
                fontSize = 13.sp,
                color = if (isSystemInDarkTheme()) BaseGrey2 else BaseBlue
            ),
            modifier = Modifier
                .padding(horizontal = Spacing24)
                .padding(top = Spacing16)
        )

        LazyRow(
            contentPadding = PaddingValues(
                horizontal = Spacing16,
                vertical = Spacing16
            ),
            state = listState
        ) {
            items(videosInfo.clips) { video ->
                VideoColumn(
                    video = video,
                    modifier = Modifier.onGloballyPositioned { coordinates ->
                        widthState.value = coordinates.size.width
                    },
                    onVideoClick = onVideoClick
                )
            }
        }
    }

    // scroll to the most relevant video
    LaunchedEffect(target) {
        val index = videosInfo.clips.indexOfFirst { it.targetIndex == target }
        if (index > 0) {
            listState.scrollToItem(index)
        }
    }
}

@Composable
private fun VideoColumn(
    video: SSVideo,
    modifier: Modifier = Modifier,
    featured: Boolean = false,
    vertical: Boolean = false,
    onVideoClick: (SSVideo) -> Unit,
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
            .padding(horizontal = Spacing8)
            .thenIf(featured) {
                Modifier.padding(
                    horizontal = Spacing24
                )
            }
            .clickable {
                onVideoClick(video)
            }
    ) {

        CoilImage(
            data = video.thumbnail,
            contentDescription = video.title,
            modifier = modifier
                .size(
                    width = size.width.dp,
                    height = size.height.dp
                ),
            cornerRadius = CoverCornerRadius
        )

        Spacer(modifier = Modifier.height(Spacing16))

        Text(
            text = video.title,
            style = TitleSmall.copy(
                color = navTitle(),
                fontSize = if (featured) 19.sp else 16.sp
            )
        )
        Text(
            text = video.artist,
            style = Body.copy(
                fontSize = 14.sp
            )
        )
    }
}

@Composable
private fun VideoRow(
    video: SSVideo,
    modifier: Modifier = Modifier,
    onVideoClick: (SSVideo) -> Unit
) {
    val size = getThumbnailSize(vertical = true)

    Row(
        horizontalArrangement = Arrangement.spacedBy(Dimens.grid_2),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(
                horizontal = Spacing24,
            )
            .clickable {
                onVideoClick(video)
            }
    ) {
        CoilImage(
            data = video.thumbnail,
            contentDescription = video.title,
            modifier = modifier
                .size(
                    width = size.width.dp,
                    height = size.height.dp
                ),
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing4)
        ) {
            Text(
                text = video.title,
                style = TitleSmall.copy(
                    color = navTitle(),
                    fontSize = 16.sp,
                    lineHeight = TextUnit.Unspecified
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = video.artist,
                style = Body.copy(
                    fontSize = 14.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private const val CoverCornerRadius = 6f

@Composable
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
