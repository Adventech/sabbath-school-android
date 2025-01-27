/*
 * Copyright (c) 2025. Adventech <info@adventech.io>
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

package ss.services.media.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.ss.design.compose.widget.material.LegacySlider
import app.ss.design.compose.widget.material.SliderDefaults
import ss.libraries.media.model.PlaybackProgressState
import kotlin.math.roundToLong

/**
 * A slider for playback progress.
 *
 * @see LegacySlider
 *
 * @param isBuffering Whether the player is buffering.
 * @param color The color of the slider.
 * @param progressState The current playback progress state.
 * @param draggingProgress The progress value being dragged to.
 * @param setDraggingProgress The callback to set the dragging progress.
 * @param height The height of the slider.
 * @param thumbRadius The radius of the thumb.
 * @param onSeekTo The callback to seek to a specific position.
 * @param modifier The modifier for the slider.
 */
@Composable
fun PlaybackSlider(
    isBuffering: Boolean,
    color: Color,
    progressState: PlaybackProgressState,
    draggingProgress: Float?,
    setDraggingProgress: (Float?) -> Unit,
    height: Dp = 56.dp,
    thumbRadius: Dp = 4.dp,
    onSeekTo: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.height(height), contentAlignment = Alignment.Center) {
        if (isBuffering) {
            LinearProgressIndicator(
                color = color,
                modifier = Modifier.fillMaxWidth().height(2.dp)
            )
        } else {
            val updatedProgressState by rememberUpdatedState(progressState)
            val updatedDraggingProgress by rememberUpdatedState(draggingProgress)

            LegacySlider(
                value = draggingProgress ?: progressState.progress,
                onValueChange = {
                    if (!isBuffering) setDraggingProgress(it)
                },
                colors = SliderDefaults.colors(
                    thumbColor = color,
                    activeTrackColor = color.copy(alpha = 0.8f),
                    inactiveTrackColor = color.copy(alpha = 0.24f),
                ),
                modifier = Modifier.fillMaxSize(),
                thumbRadius = thumbRadius,
                onValueChangeFinished = {
                    val position = (updatedProgressState.total.toFloat() * (updatedDraggingProgress ?: 0f)).roundToLong()
                    onSeekTo(position)
                    setDraggingProgress(null)
                }
            )
        }
    }

}
