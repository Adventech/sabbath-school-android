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
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
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
import app.ss.design.compose.extensions.isS
import app.ss.design.compose.theme.Spacing16
import app.ss.design.compose.theme.Spacing4
import app.ss.design.compose.theme.Spacing8
import app.ss.design.compose.theme.onSurfaceSecondary
import app.ss.media.playback.extensions.millisToDuration
import app.ss.media.playback.model.PlaybackProgressState
import com.cryart.design.theme.BaseGrey1
import com.cryart.design.theme.BaseGrey3
import com.cryart.design.theme.OffWhite
import com.cryart.design.theme.darker
import com.cryart.design.widgets.material.Slider
import com.cryart.design.widgets.material.SliderDefaults
import kotlin.math.roundToLong

private object ProgressColors {
    @Composable
    fun thumbColor(forceDark: Boolean): Color {
        return when {
            isS() -> MaterialTheme.colorScheme.onSurfaceVariant
            isSystemInDarkTheme() -> BaseGrey1
            forceDark -> BaseGrey1
            else -> OffWhite.darker()
        }
    }

    @Composable
    fun activeTrackColor(forceDark: Boolean): Color = thumbColor(forceDark)

    @Composable
    fun inactiveTrackColor(forceDark: Boolean): Color {
        return when {
            isS() -> MaterialTheme.colorScheme.inverseOnSurface
            isSystemInDarkTheme() -> BaseGrey3
            forceDark -> BaseGrey3
            else -> BaseGrey1
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
            horizontal = Spacing16,
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
            .padding(horizontal = Spacing4),
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
    draggingProgress: Float?,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .padding(top = Spacing8)
            .padding(horizontal = Spacing4)
    ) {
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            val currentDuration = when (draggingProgress != null) {
                true -> (progressState.total.toFloat() * (draggingProgress)).toLong().millisToDuration()
                else -> progressState.currentDuration
            }
            val textStyle = MaterialTheme.typography.titleSmall.copy(
                fontSize = 14.sp
            )
            Text(
                currentDuration,
                style = textStyle,
                color = onSurfaceSecondary(),
            )
            Text(
                progressState.totalDuration,
                style = textStyle,
                color = onSurfaceSecondary(),
            )
        }
    }
}
