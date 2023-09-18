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

package app.ss.media.playback.ui.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.material.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import app.ss.design.compose.extensions.previews.DayNightPreviews
import app.ss.design.compose.theme.SsTheme
import app.ss.media.playback.model.PlaybackSpeed

@Composable
internal fun PlaybackSpeedLabel(
    playbackSpeed: PlaybackSpeed,
    toggleSpeed: () -> Unit,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = {
            toggleSpeed()
        },
        modifier = modifier
    ) {
        AnimatedContent(
            targetState = playbackSpeed,
            transitionSpec = {
                if (targetState.speed > initialState.speed) {
                    slideInVertically(initialOffsetY = { height -> height }) + fadeIn() togetherWith
                        slideOutVertically(targetOffsetY = { height -> -height }) + fadeOut()
                } else {
                    slideInVertically(initialOffsetY = { height -> -height }) + fadeIn() togetherWith
                        slideOutVertically(targetOffsetY = { height -> height }) + fadeOut()
                }.using(
                    SizeTransform(clip = false)
                )
            }, label = ""
        ) { targetSpeed ->
            Text(
                text = targetSpeed.label,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 18.sp
                ),
                color = contentColor
            )
        }
    }
}

@DayNightPreviews
@Composable
private fun Preview() {
    SsTheme {
        Surface {
            PlaybackSpeedLabel(
                playbackSpeed = PlaybackSpeed.NORMAL,
                toggleSpeed = {  },
                contentColor = LocalContentColor.current
            )
        }
    }
}
