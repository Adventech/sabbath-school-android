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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import app.ss.design.compose.extensions.color.darker
import app.ss.design.compose.extensions.isS
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.theme.color.SsColors
import app.ss.design.compose.widget.material.Slider
import app.ss.design.compose.widget.material.SliderDefaults
import app.ss.media.playback.extensions.millisToDuration
import app.ss.media.playback.model.PlaybackProgressState
import kotlin.math.roundToLong

private object ProgressColors {
    @Composable
    fun thumbColor(forceDark: Boolean): Color {
        return when {
            isS() -> MaterialTheme.colorScheme.onSurfaceVariant
            SsTheme.colors.isDark -> SsColors.BaseGrey1
            forceDark -> SsColors.BaseGrey1
            else -> SsColors.OffWhite.darker()
        }
    }

    @Composable
    fun activeTrackColor(forceDark: Boolean): Color = thumbColor(forceDark)

    @Composable
    fun inactiveTrackColor(forceDark: Boolean): Color {
        return when {
            isS() -> MaterialTheme.colorScheme.inverseOnSurface
            SsTheme.colors.isDark -> SsColors.BaseGrey3
            forceDark -> SsColors.BaseGrey3
            else -> SsColors.BaseGrey1
        }
    }
}

@Composable
internal fun PlaybackProgressDuration(
    isBuffering: Boolean,
    progressState: PlaybackProgressState,
    onSeekTo: (Long) -> Unit,
    modifier: Modifier = Modifier,
    forceDark: Boolean = false
) {
    val (draggingProgress, setDraggingProgress) = remember { mutableStateOf<Float?>(null) }

    Box(
        modifier = modifier.padding(
            horizontal = 16.dp
        )
    ) {
        PlaybackProgressSlider(
            isBuffering = isBuffering,
            progressState,
            draggingProgress,
            setDraggingProgress,
            forceDark = forceDark,
            onSeekTo = onSeekTo
        )
        PlaybackProgressDuration(
            progressState,
            draggingProgress
        )
    }
}

@Composable
private fun BoxScope.PlaybackProgressSlider(
    isBuffering: Boolean,
    progressState: PlaybackProgressState,
    draggingProgress: Float?,
    setDraggingProgress: (Float?) -> Unit,
    height: Dp = 56.dp,
    thumbRadius: Dp = 4.dp,
    forceDark: Boolean = false,
    onSeekTo: (Long) -> Unit
) {
    val updatedProgressState by rememberUpdatedState(progressState)
    val updatedDraggingProgress by rememberUpdatedState(draggingProgress)

    val sliderColors = SliderDefaults.colors(
        thumbColor = ProgressColors.thumbColor(forceDark),
        activeTrackColor = ProgressColors.activeTrackColor(forceDark),
        inactiveTrackColor = ProgressColors.inactiveTrackColor(forceDark)
    )

    Slider(
        value = draggingProgress ?: progressState.progress,
        onValueChange = {
            if (!isBuffering) setDraggingProgress(it)
        },
        colors = sliderColors,
        thumbRadius = thumbRadius,
        modifier = Modifier
            .height(height)
            .align(Alignment.TopCenter)
            .padding(horizontal = 4.dp),
        onValueChangeFinished = {
            val position = (updatedProgressState.total.toFloat() * (updatedDraggingProgress ?: 0f)).roundToLong()
            onSeekTo(position)
            setDraggingProgress(null)
        }
    )
}

@Composable
private fun BoxScope.PlaybackProgressDuration(
    progressState: PlaybackProgressState,
    draggingProgress: Float?
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .padding(top = 8.dp)
            .padding(horizontal = 4.dp)
    ) {
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            val currentDuration = when (draggingProgress != null) {
                true -> (progressState.total.toFloat() * (draggingProgress)).toLong().millisToDuration()
                else -> progressState.currentDuration
            }
            val textStyle = SsTheme.typography.titleSmall.copy(
                fontSize = 14.sp
            )
            Text(
                currentDuration,
                style = textStyle,
                color = SsTheme.colors.onSurfaceSecondary
            )
            Text(
                progressState.totalDuration,
                style = textStyle,
                color = SsTheme.colors.onSurfaceSecondary
            )
        }
    }
}
