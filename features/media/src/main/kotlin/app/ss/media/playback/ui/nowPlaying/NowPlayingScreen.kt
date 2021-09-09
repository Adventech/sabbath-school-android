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

package app.ss.media.playback.ui.nowPlaying

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.ss.media.R
import app.ss.media.playback.extensions.NONE_PLAYBACK_STATE
import app.ss.media.playback.extensions.isPlaying
import app.ss.media.playback.model.AudioFile
import app.ss.media.playback.model.PlaybackQueue
import app.ss.media.playback.model.PlaybackSpeed
import app.ss.media.playback.ui.common.rememberFlowWithLifecycle
import app.ss.media.playback.ui.nowPlaying.ScreenDefaults.tintColor
import app.ss.media.playback.ui.nowPlaying.components.BoxState
import app.ss.media.playback.ui.nowPlaying.components.NowPlayingBox
import app.ss.media.playback.ui.nowPlaying.components.PlayBackControls
import app.ss.media.playback.ui.nowPlaying.components.PlaybackProgress
import app.ss.media.playback.ui.nowPlaying.components.PlaybackQueueList
import app.ss.media.playback.ui.playbackContentColor
import com.cryart.design.ext.thenIf
import com.cryart.design.theme.BaseBlue
import com.cryart.design.theme.BaseGrey2
import com.cryart.design.theme.Dimens
import com.cryart.design.theme.Spacing16
import com.cryart.design.theme.Spacing24
import com.cryart.design.theme.TitleMedium
import com.cryart.design.widgets.DragHandle
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private object ScreenDefaults {

    @Composable
    fun tintColor(): Color =
        if (isSystemInDarkTheme()) {
            BaseGrey2
        } else {
            BaseBlue
        }
}

@Composable
internal fun NowPlayingScreen(
    viewModel: NowPlayingViewModel = viewModel(),
    isAtTop: (Boolean) -> Unit = {},
) {
    val listState = rememberLazyListState()
    val playbackConnection = viewModel.playbackConnection
    val playbackState by rememberFlowWithLifecycle(playbackConnection.playbackState)
        .collectAsState(NONE_PLAYBACK_STATE)
    val nowPlaying by rememberFlowWithLifecycle(viewModel.nowPlayingAudio)
        .collectAsState(AudioFile(""))
    val playbackQueue by rememberFlowWithLifecycle(playbackConnection.playbackQueue)
        .collectAsState(PlaybackQueue())
    val nowPlayingAudio = if (nowPlaying.id.isEmpty()) {
        playbackQueue.currentAudio ?: nowPlaying
    } else {
        nowPlaying
    }

    var boxState by remember { mutableStateOf(BoxState.Expanded) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = Spacing16),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DragHandle()

        val expanded = boxState == BoxState.Expanded
        val spacing by animateDpAsState(
            if (expanded) Spacing24 else 0.dp
        )

        Spacer(
            modifier = Modifier
                .thenIf(expanded) {
                    Modifier.weight(1f)
                }
                .thenIf(expanded.not()) {
                    Modifier.height(Dimens.grid_2)
                }
        )

        NowPlayingBox(
            audio = nowPlayingAudio,
            boxState = boxState
        )

        PlaybackQueue(
            expanded = expanded,
            listState = listState,
            playbackQueue = playbackQueue,
            isPlaying = playbackState.isPlaying,
            nowPlayingId = nowPlaying.id,
            onPlayAudio = { position ->
                playbackConnection.transportControls?.skipToQueueItem(position.toLong())
            }
        )

        PlaybackProgress(
            playbackState = playbackState,
            playbackConnection = playbackConnection
        )

        Spacer(modifier = Modifier.height(spacing))

        PlayBackControls(
            playbackState = playbackState,
            contentColor = playbackContentColor(),
            playbackConnection = playbackConnection
        )

        Spacer(modifier = Modifier.height(spacing))

        BottomControls(
            playbackSpeedFlow = playbackConnection.playbackSpeed,
            toggleSpeed = { playbackSpeed ->
                playbackConnection.toggleSpeed(playbackSpeed)
            },
            toggleExpand = {
                boxState = when (boxState) {
                    BoxState.Collapsed -> BoxState.Expanded
                    BoxState.Expanded -> BoxState.Collapsed
                }
            }
        )
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .map { index -> boxState == BoxState.Expanded || index == 0 }
            .distinctUntilChanged()
            .collect { isAtTop(it) }
    }
}

@Composable
private fun ColumnScope.PlaybackQueue(
    expanded: Boolean,
    playbackQueue: PlaybackQueue,
    listState: LazyListState,
    isPlaying: Boolean,
    nowPlayingId: String,
    onPlayAudio: (Int) -> Unit,
) {

    val alpha by animateFloatAsState(
        targetValue = if (expanded) 0f else 1f,
    )

    PlaybackQueueList(
        listState = listState,
        modifier = Modifier
            .weight(1f)
            .alpha(alpha)
            .padding(top = Spacing16),
        playbackQueue = playbackQueue.audiosList,
        nowPlayingId = nowPlayingId,
        isPlaying = isPlaying,
        onPlayAudio = onPlayAudio
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun BottomControls(
    modifier: Modifier = Modifier,
    playbackSpeedFlow: StateFlow<PlaybackSpeed>,
    toggleSpeed: (PlaybackSpeed) -> Unit = {},
    toggleExpand: () -> Unit = {}
) {
    val playbackSpeed by rememberFlowWithLifecycle(playbackSpeedFlow)
        .collectAsState(PlaybackSpeed.NORMAL)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                end = Dimens.grid_4,
                start = Dimens.grid_2
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextButton(
            onClick = {
                toggleSpeed(playbackSpeed)
            }
        ) {
            AnimatedContent(
                targetState = playbackSpeed,
                transitionSpec = {
                    if (targetState.speed > initialState.speed) {
                        slideInVertically({ height -> height }) + fadeIn() with
                            slideOutVertically({ height -> -height }) + fadeOut()
                    } else {
                        slideInVertically({ height -> -height }) + fadeIn() with
                            slideOutVertically({ height -> height }) + fadeOut()
                    }.using(
                        SizeTransform(clip = false)
                    )
                }
            ) { targetSpeed ->
                Text(
                    text = targetSpeed.label,
                    style = TitleMedium.copy(
                        color = tintColor()
                    )
                )
            }
        }

        IconButton(onClick = toggleExpand) {
            Icon(
                painter = painterResource(id = R.drawable.ic_audio_icon_playlist),
                contentDescription = "PlayList",
                tint = tintColor()
            )
        }
    }
}
