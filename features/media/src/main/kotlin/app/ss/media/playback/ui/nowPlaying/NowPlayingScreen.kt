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

import android.support.v4.media.session.PlaybackStateCompat
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.ss.media.R
import app.ss.media.playback.PlaybackConnection
import app.ss.media.playback.extensions.NONE_PLAYBACK_STATE
import app.ss.media.playback.extensions.NONE_PLAYING
import app.ss.media.playback.extensions.isBuffering
import app.ss.media.playback.extensions.millisToDuration
import app.ss.media.playback.model.PlaybackProgressState
import app.ss.media.playback.model.PlaybackSpeed
import app.ss.media.playback.ui.common.Delayed
import app.ss.media.playback.ui.common.rememberFlowWithLifecycle
import app.ss.media.playback.ui.nowPlaying.ScreenDefaults.tintColor
import app.ss.media.playback.ui.nowPlaying.components.BoxState
import app.ss.media.playback.ui.nowPlaying.components.NowPlayingBox
import com.cryart.design.base.TransparentBottomSheetSurface
import com.cryart.design.theme.BaseBlue
import com.cryart.design.theme.BaseGrey2
import com.cryart.design.theme.Dimens
import com.cryart.design.theme.Spacing16
import com.cryart.design.theme.Spacing24
import com.cryart.design.theme.TitleMedium
import com.cryart.design.widgets.DragHandle
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.roundToLong

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
) {
    TransparentBottomSheetSurface {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = Spacing16),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val playbackConnection = viewModel.playbackConnection
            val playbackState by rememberFlowWithLifecycle(playbackConnection.playbackState)
                .collectAsState(NONE_PLAYBACK_STATE)
            val nowPlaying by rememberFlowWithLifecycle(playbackConnection.nowPlaying)
                .collectAsState(NONE_PLAYING)
            val audio by rememberFlowWithLifecycle(flow = viewModel.nowPlaying)
                .collectAsState(initial = null)
            var boxState by remember { mutableStateOf(BoxState.Expanded) }

            DragHandle()

            Spacer(modifier = Modifier.height(Dimens.grid_4))

            if (audio != null) {
                NowPlayingBox(
                    audio = audio!!,
                    boxState = boxState
                )
            }

            PlaybackProgress(
                playbackState = playbackState,
                contentColor = MaterialTheme.colors.primary,
                playbackConnection = playbackConnection
            )

            Spacer(modifier = Modifier.weight(1f))

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
    }
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
                horizontal = Dimens.grid_4,
            ),
        verticalAlignment = Alignment.CenterVertically
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

        Spacer(modifier = Modifier.weight(1f))

        IconButton(onClick = toggleExpand) {
            Icon(
                painter = painterResource(id = R.drawable.ic_audio_icon_playlist),
                contentDescription = "PlayList",
                tint = tintColor()
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun PlaybackProgress(
    playbackState: PlaybackStateCompat,
    contentColor: Color,
    playbackConnection: PlaybackConnection
) {
    val progressState by rememberFlowWithLifecycle(playbackConnection.playbackProgress)
        .collectAsState(PlaybackProgressState())

    val (draggingProgress, setDraggingProgress) = remember { mutableStateOf<Float?>(null) }

    Box(
        modifier = Modifier.padding(
            horizontal = Dimens.grid_4,
            vertical = Spacing16
        )
    ) {
        PlaybackProgressSlider(
            playbackState,
            progressState,
            draggingProgress,
            setDraggingProgress,
            contentColor,
            playbackConnection = playbackConnection
        )
        PlaybackProgressDuration(
            progressState,
            draggingProgress
        )
    }
}

@Composable
private fun PlaybackProgressSlider(
    playbackState: PlaybackStateCompat,
    progressState: PlaybackProgressState,
    draggingProgress: Float?,
    setDraggingProgress: (Float?) -> Unit,
    contentColor: Color,
    bufferedProgressColor: Color = contentColor.copy(alpha = 0.25f),
    height: Dp = 44.dp,
    playbackConnection: PlaybackConnection
) {
    val updatedProgressState by rememberUpdatedState(progressState)
    val updatedDraggingProgress by rememberUpdatedState(draggingProgress)

    val sliderColors = SliderDefaults.colors(
        thumbColor = contentColor,
        activeTrackColor = contentColor,
        inactiveTrackColor = contentColor.copy(alpha = ContentAlpha.disabled)
    )
    val linearProgressMod = Modifier
        .fillMaxWidth(fraction = .99f)
        .clip(CircleShape)

    val bufferedProgress = progressState.bufferedProgress
    val isBuffering = playbackState.isBuffering

    Box(
        modifier = Modifier.height(height),
        contentAlignment = Alignment.Center
    ) {
        if (!isBuffering)
            LinearProgressIndicator(
                progress = bufferedProgress,
                color = bufferedProgressColor,
                backgroundColor = Color.Transparent,
                modifier = linearProgressMod
            )

        Slider(
            value = draggingProgress ?: progressState.progress,
            onValueChange = {
                if (!isBuffering) setDraggingProgress(it)
            },
            colors = sliderColors,
            modifier = Modifier.alpha(
                if (isBuffering) 0f else 1f
            ),
            onValueChangeFinished = {
                playbackConnection.transportControls?.seekTo(
                    (updatedProgressState.total.toFloat() * (updatedDraggingProgress ?: 0f)).roundToLong()
                )
                setDraggingProgress(null)
            }
        )

        if (isBuffering) {
            LinearProgressIndicator(
                progress = 0f,
                color = contentColor,
                modifier = linearProgressMod
            )
            Delayed(
                modifier = Modifier
                    .align(Alignment.Center)
                    .then(linearProgressMod)
            ) {
                LinearProgressIndicator(
                    color = contentColor,
                )
            }
        }
    }
}

@Composable
private fun BoxScope.PlaybackProgressDuration(
    progressState: PlaybackProgressState,
    draggingProgress: Float?,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Spacing24)
            .align(Alignment.BottomCenter)
    ) {
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            val currentDuration = when (draggingProgress != null) {
                true -> (progressState.total.toFloat() * (draggingProgress)).toLong().millisToDuration()
                else -> progressState.currentDuration
            }
            Text(
                currentDuration, style = MaterialTheme.typography.caption,
            )
            Text(
                progressState.totalDuration, style = MaterialTheme.typography.caption,
            )
        }
    }
}
