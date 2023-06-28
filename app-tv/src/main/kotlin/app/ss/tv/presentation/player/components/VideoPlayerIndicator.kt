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

package app.ss.tv.presentation.player.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import app.ss.tv.presentation.extentions.handleDPadKeyEvents
import app.ss.tv.presentation.player.VideoPlayerState

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RowScope.VideoPlayerControllerIndicator(
    progress: Float,
    videoPlayerState: VideoPlayerState,
    onSeek: (seekProgress: Float) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    var isSelected by remember { mutableStateOf(false) }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val color by rememberUpdatedState(
        newValue = if (isSelected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurface
    )
    val animatedIndicatorHeight by animateDpAsState(
        targetValue = 4.dp.times((if (isFocused) 2.5f else 1f))
    )
    var seekProgress by remember { mutableFloatStateOf(0f) }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(isSelected) {
        if (isSelected) {
            videoPlayerState.showControls(indefinite = true)
        } else {
            videoPlayerState.showControls()
        }
    }

    Canvas(
        modifier = Modifier
            .weight(1f)
            .height(animatedIndicatorHeight)
            .padding(horizontal = 4.dp)
            .handleDPadKeyEvents(
                onEnter = {
                    if (isSelected) {
                        onSeek(seekProgress)
                        focusManager.moveFocus(FocusDirection.Exit)
                    } else {
                        seekProgress = progress
                    }
                    isSelected = !isSelected
                },
                onLeft = {
                    if (isSelected) {
                        seekProgress -= 0.1f
                    } else {
                        focusManager.moveFocus(FocusDirection.Left)
                    }
                },
                onRight = {
                    if (isSelected) {
                        seekProgress += 0.1f
                    } else {
                        focusManager.moveFocus(FocusDirection.Right)
                    }
                }
            )
            .focusable(interactionSource = interactionSource),
        onDraw = {
            val yOffset = size.height.div(2)
            drawLine(
                color = color.copy(alpha = 0.24f),
                start = Offset(x = 0f, y = yOffset),
                end = Offset(x = size.width, y = yOffset),
                strokeWidth = size.height,
                cap = StrokeCap.Round
            )
            drawLine(
                color = color,
                start = Offset(x = 0f, y = yOffset),
                end = Offset(
                    x = size.width.times(if (isSelected) seekProgress else progress),
                    y = yOffset
                ),
                strokeWidth = size.height,
                cap = StrokeCap.Round
            )
        }
    )
}
