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

package io.adventech.blockkit.ui.media

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.delay
import ss.libraries.media.api.DEFAULT_FORWARD
import ss.libraries.media.api.DEFAULT_REWIND
import ss.libraries.media.api.PLAYBACK_PROGRESS_INTERVAL
import ss.libraries.media.model.PlaybackProgressState
import ss.services.media.ui.spec.PlaybackStateSpec

@Composable
fun MediaPlayer(
    source: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.(ExoPlayer, PlaybackStateSpec, PlaybackProgressState, () -> Unit, (Long) -> Unit) -> Unit,
) {
    val context = LocalContext.current

    var playbackState by remember(source) {
        mutableStateOf(
            PlaybackStateSpec.NONE.copy(
                isPlayEnabled = true,
                canShowMini = false,
            )
        )
    }
    var progressState by remember(source) {
        mutableStateOf(PlaybackProgressState())
    }

    val exoPlayer = remember(context, source) {
        ExoPlayer.Builder(context)
            .setSeekBackIncrementMs(DEFAULT_REWIND)
            .setSeekForwardIncrementMs(DEFAULT_FORWARD)
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

    DisposableEffect(source) {
        exoPlayer.setMediaItem(MediaItem.fromUri(source))
        exoPlayer.prepare()

        onDispose { exoPlayer.release() }
    }

    LaunchedEffect(playbackState.isPlaying) {
        while (playbackState.isPlaying) {
            delay(PLAYBACK_PROGRESS_INTERVAL)
            progressState = progressState.copy(
                position = exoPlayer.currentPosition,
            )
        }
    }

    Column(modifier = modifier) {
        content(
            exoPlayer,
            playbackState,
            progressState,
            {
                if (exoPlayer.isPlaying) {
                    exoPlayer.pause()
                } else {
                    if (exoPlayer.currentPosition == exoPlayer.duration) {
                        exoPlayer.seekTo(0)
                    }
                    exoPlayer.play()
                }
            },
            { position ->
                progressState = progressState.copy(
                    position = position,
                )
                exoPlayer.seekTo(position)
            }
        )
    }
}
