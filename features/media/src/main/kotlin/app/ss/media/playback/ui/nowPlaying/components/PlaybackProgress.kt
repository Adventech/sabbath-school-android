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

package app.ss.media.playback.ui.nowPlaying.components

import android.support.v4.media.session.PlaybackStateCompat
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ss.media.playback.PlaybackConnection
import app.ss.media.playback.extensions.isBuffering
import app.ss.media.playback.extensions.millisToDuration
import app.ss.media.playback.model.PlaybackProgressState
import app.ss.media.playback.ui.common.rememberFlowWithLifecycle
import com.cryart.design.theme.BaseGrey1
import com.cryart.design.theme.BaseGrey2
import com.cryart.design.theme.BaseGrey3
import com.cryart.design.theme.Dimens
import com.cryart.design.theme.OffWhite
import com.cryart.design.theme.Spacing8
import com.cryart.design.theme.TitleSmall
import com.cryart.design.theme.darker
import kotlin.math.roundToLong

private object ProgressColors {
    @Composable
    fun thumbColor(): Color =
        if (isSystemInDarkTheme()) {
            BaseGrey1
        } else {
            OffWhite.darker()
        }

    @Composable
    fun activeTrackColor(): Color = thumbColor()

    @Composable
    fun inactiveTrackColor(): Color =
        if (isSystemInDarkTheme()) {
            BaseGrey3
        } else {
            BaseGrey1
        }
}

@Composable
internal fun PlaybackProgress(
    playbackState: PlaybackStateCompat,
    playbackConnection: PlaybackConnection
) {
    val progressState by rememberFlowWithLifecycle(playbackConnection.playbackProgress)
        .collectAsState(PlaybackProgressState())

    val (draggingProgress, setDraggingProgress) = remember { mutableStateOf<Float?>(null) }

    Box(
        modifier = Modifier.padding(
            horizontal = Dimens.grid_4,
        )
    ) {
        PlaybackProgressSlider(
            playbackState,
            progressState,
            draggingProgress,
            setDraggingProgress,
            playbackConnection = playbackConnection
        )
        PlaybackProgressDuration(
            progressState,
            draggingProgress
        )
    }
}

@Composable
private fun BoxScope.PlaybackProgressSlider(
    playbackState: PlaybackStateCompat,
    progressState: PlaybackProgressState,
    draggingProgress: Float?,
    setDraggingProgress: (Float?) -> Unit,
    height: Dp = 60.dp,
    playbackConnection: PlaybackConnection
) {
    val updatedProgressState by rememberUpdatedState(progressState)
    val updatedDraggingProgress by rememberUpdatedState(draggingProgress)

    val sliderColors = SliderDefaults.colors(
        thumbColor = ProgressColors.thumbColor(),
        activeTrackColor = ProgressColors.activeTrackColor(),
        inactiveTrackColor = ProgressColors.inactiveTrackColor()
    )
    val isBuffering = playbackState.isBuffering

    Slider(
        value = draggingProgress ?: progressState.progress,
        onValueChange = {
            if (!isBuffering) setDraggingProgress(it)
        },
        colors = sliderColors,
        modifier = Modifier
            .height(height)
            .align(Alignment.TopCenter),
        onValueChangeFinished = {
            playbackConnection.transportControls?.seekTo(
                (updatedProgressState.total.toFloat() * (updatedDraggingProgress ?: 0f)).roundToLong()
            )
            setDraggingProgress(null)
        }
    )
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
            .align(Alignment.BottomCenter)
            .padding(top = Spacing8)
            .padding(horizontal = Spacing8)
    ) {
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            val currentDuration = when (draggingProgress != null) {
                true -> (progressState.total.toFloat() * (draggingProgress)).toLong().millisToDuration()
                else -> progressState.currentDuration
            }
            val textStyle = TitleSmall.copy(
                color = BaseGrey2,
                fontSize = 14.sp
            )
            Text(
                currentDuration,
                style = textStyle,
            )
            Text(
                progressState.totalDuration,
                style = textStyle,
            )
        }
    }
}
