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

package io.adventech.blockkit.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreTime
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.PlayerSurface
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import io.adventech.blockkit.model.BlockItem
import io.adventech.blockkit.ui.media.MediaPlayer
import io.adventech.blockkit.ui.style.LatoFontFamily
import io.adventech.blockkit.ui.style.Styler
import io.adventech.blockkit.ui.style.theme.BlocksPreviewTheme
import io.adventech.blockkit.ui.style.thenIf
import kotlinx.coroutines.delay
import me.saket.cascade.CascadeDropdownMenu
import me.saket.cascade.CascadeDropdownMenuItem
import me.saket.cascade.rememberCascadeState
import ss.libraries.media.model.PlaybackProgressState
import ss.libraries.media.model.extensions.millisToDuration
import ss.services.media.ui.PlaybackPlayPause
import ss.services.media.ui.common.PlaybackSlider
import ss.services.media.ui.spec.PlaybackSpeed
import ss.services.media.ui.spec.PlaybackStateSpec

@Composable
fun VideoContent(blockItem: BlockItem.Video, modifier: Modifier = Modifier) {
    val textStyle = Styler.textStyle(null)
    MediaPlayer(
        source = blockItem.src,
        modifier = modifier,
    ) { exoplayer, playbackState, progressState, onPlayPause, onSeekTo ->

        PlayerContent(
            exoPlayer = exoplayer,
            playbackState = playbackState,
            progressState = progressState,
            modifier = Modifier,
            onPlayPause = onPlayPause,
            onSeekTo = onSeekTo,
        )

        blockItem.caption?.let {
            Text(
                text = it,
                modifier = Modifier.fillMaxWidth(),
                style = textStyle.copy(
                    fontStyle = FontStyle.Italic,
                    color = textStyle.color.copy(alpha = 0.7f),
                ),
                textAlign = TextAlign.Center
            )
        }

    }
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
private fun PlayerContent(
    exoPlayer: ExoPlayer,
    playbackState: PlaybackStateSpec,
    progressState: PlaybackProgressState,
    modifier: Modifier = Modifier,
    onPlayPause: () -> Unit = {},
    onSeekTo: (Long) -> Unit = {},
) {
    val hazeState = remember { HazeState() }
    var isControlVisible by remember { mutableStateOf(!playbackState.isPlaying) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16 / 9f)
            .clip(Styler.roundedShape()),
    ) {
        PlayerSurface(
            player = exoPlayer,
            modifier = Modifier
                .hazeSource(hazeState)
                .fillMaxSize()
                .clickable { isControlVisible = !isControlVisible },
        )

        VideoControls(
            visible = isControlVisible,
            playbackState = playbackState,
            progressState = progressState,
            modifier = Modifier
                .fillMaxSize()
                .thenIf(isControlVisible) {
                    hazeEffect(hazeState, HazeMaterials.ultraThin(containerColor = Color.Black))
                }
            ,
            onPlayPause = onPlayPause,
            onSeekTo = onSeekTo,
        )
    }

    LaunchedEffect(isControlVisible, playbackState) {
        if (isControlVisible && playbackState.isPlaying) {
            delay(3000)
            isControlVisible = false
        } else {
            isControlVisible = !playbackState.isPlaying
        }
    }
}

@Composable
private fun VideoControls(
    visible: Boolean,
    playbackState: PlaybackStateSpec,
    progressState: PlaybackProgressState,
    modifier: Modifier = Modifier,
    onPlayPause: () -> Unit = {},
    onSeekTo: (Long) -> Unit = {},
) {
    val (draggingProgress, setDraggingProgress) = remember { mutableStateOf<Float?>(null) }
    val currentDuration = when (draggingProgress != null) {
        true -> (progressState.total.toFloat() * (draggingProgress)).toLong().millisToDuration()
        else -> progressState.currentDuration
    }
    val contentColor = Color.White
    var isMenuShown by rememberSaveable { mutableStateOf(false) }
    val cascadeState = rememberCascadeState()
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    Box(
        modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(
            visible = visible,
            modifier = Modifier.size(64.dp)
        ) {
            PlaybackPlayPause(
                spec = playbackState,
                contentColor = contentColor,
                onPlayPause = onPlayPause,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .size(64.dp)
                    .align(Alignment.Center),
                iconSize = 48.dp,
            )
        }

        AnimatedVisibility(
            visible = visible,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .align(Alignment.BottomCenter),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = currentDuration,
                    color = contentColor,
                    fontSize = 11.sp,
                    fontFamily = LatoFontFamily
                )

                PlaybackSlider(
                    isBuffering = playbackState.isBuffering,
                    color = contentColor,
                    progressState = progressState,
                    draggingProgress = draggingProgress,
                    setDraggingProgress = setDraggingProgress,
                    onSeekTo = onSeekTo,
                    modifier = Modifier.weight(1f),
                )

                Text(
                    text = progressState.totalDuration,
                    color = contentColor,
                    fontSize = 11.sp,
                    fontFamily = LatoFontFamily
                )

                IconButton(
                    onClick = { isMenuShown = !isMenuShown },
                    modifier = Modifier,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoreTime,
                        contentDescription = null,
                        modifier = Modifier,
                        tint = contentColor
                    )
                }
            }
        }
    }

    CascadeDropdownMenu(
        expanded = isMenuShown,
        onDismissRequest = { isMenuShown = false },
        modifier = Modifier,
        state = cascadeState,
        offset = DpOffset(screenWidth, 0.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        CascadeDropdownMenuItem(
            text = { Text("Playback Speed") },
            children = {
                PlaybackSpeed.entries.forEach {
                    DropdownMenuItem(
                        text = { Text(it.label) },
                        onClick = { isMenuShown = false },
                    )
                }
            },
            cascadeState = cascadeState,
        )
        DropdownMenuItem(
            text = { Text("Audio") },
            onClick = { isMenuShown = false },
        )
    }
}

@Preview
@Composable
private fun Preview() {
    BlocksPreviewTheme {
        Surface(color = Color.Black) {
            VideoControls(
                visible = true,
                playbackState = PlaybackStateSpec.NONE.copy(isPlaying = true),
                progressState = PlaybackProgressState(
                    total = 10000L,
                    position = 3000L,
                ),
                onPlayPause = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16 / 9f)
                    .clip(Styler.roundedShape()),
            )
        }
    }
}
