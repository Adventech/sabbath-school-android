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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import io.adventech.blockkit.model.BlockItem
import io.adventech.blockkit.ui.style.Styler
import io.adventech.blockkit.ui.style.theme.BlocksPreviewTheme
import kotlinx.coroutines.delay
import ss.libraries.media.api.PLAYBACK_PROGRESS_INTERVAL
import ss.libraries.media.model.PlaybackProgressState
import ss.libraries.media.model.extensions.millisToDuration
import ss.services.media.ui.PlaybackPlayPause
import ss.services.media.ui.common.PlaybackSlider
import ss.services.media.ui.spec.PlaybackStateSpec

@Composable
fun AudioContent(blockItem: BlockItem.Audio, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    var playbackState by remember(blockItem) {
        mutableStateOf(
            PlaybackStateSpec.NONE.copy(
                isPlayEnabled = true,
                canShowMini = false,
            )
        )
    }
    var progressState by remember(blockItem) {
        mutableStateOf(PlaybackProgressState())
    }

    val exoPlayer = remember(context, blockItem) {
        ExoPlayer.Builder(context)
            .build().apply {
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        super.onPlaybackStateChanged(state)
                        playbackState = playbackState.copy(
                            isBuffering = state == Player.STATE_BUFFERING,
                        )

                        if (state == Player.STATE_READY) {
                            progressState = progressState.copy(
                                total = this@apply.duration
                            )
                        }
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        super.onIsPlayingChanged(isPlaying)
                        playbackState = playbackState.copy(
                            isPlaying = isPlaying,
                        )
                    }
                })
            }
    }

    DisposableEffect(blockItem) {
        exoPlayer.setMediaItem(MediaItem.fromUri(blockItem.src))
        exoPlayer.prepare()

        onDispose { exoPlayer.release() }
    }

    PlayerContent(
        playbackState = playbackState,
        progressState = progressState,
        modifier = modifier,
        onPlayPause = {
            if (exoPlayer.isPlaying) {
                exoPlayer.pause()
            } else {
                exoPlayer.play()
            }
        },
        onSeekTo = {
            Snapshot.withMutableSnapshot {
                progressState = progressState.copy(
                    position = it,
                )
                exoPlayer.seekTo(it)
            }
        }
    )

    LaunchedEffect(playbackState.isPlaying) {
        while (playbackState.isPlaying) {
            delay(PLAYBACK_PROGRESS_INTERVAL)
            progressState = progressState.copy(
                position = exoPlayer.currentPosition,
            )
        }
    }
}

@Composable
private fun PlayerContent(
    playbackState: PlaybackStateSpec,
    progressState: PlaybackProgressState,
    modifier: Modifier = Modifier,
    onPlayPause: () -> Unit = {},
    onSeekTo: (Long) -> Unit = {},
) {
    val textColor = Styler.textColor(null)

    val (draggingProgress, setDraggingProgress) = remember { mutableStateOf<Float?>(null) }

    val currentDuration = when (draggingProgress != null) {
        true -> (progressState.total.toFloat() * (draggingProgress)).toLong().millisToDuration()
        else -> progressState.currentDuration
    }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Styler.genericBackgroundColorForInteractiveBlock(),
            contentColor = textColor
        ),
        shape = Styler.roundedShape(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlaybackPlayPause(
                spec = playbackState,
                contentColor = textColor,
                onPlayPause = onPlayPause,
            )

            Text(
                currentDuration,
                style = Styler.textStyle(null).copy(
                    fontSize = 14.sp
                ),
                color = textColor
            )

            PlaybackSlider(
                isBuffering = playbackState.isBuffering,
                color = textColor,
                progressState = progressState,
                draggingProgress = draggingProgress,
                setDraggingProgress = setDraggingProgress,
                onSeekTo = onSeekTo,
                modifier = Modifier.weight(1f),
            )

            Spacer(Modifier.size(4.dp))
        }
    }
}

@Composable
@PreviewLightDark
private fun Preview() {
    BlocksPreviewTheme {
        Surface {
            PlayerContent(
                playbackState = PlaybackStateSpec.NONE.copy(
                    isPlayEnabled = true
                ),
                progressState = PlaybackProgressState(
                    total = 3 * 60 * 1000,
                    position = 60 * 1000
                ),
            )
        }
    }
}
