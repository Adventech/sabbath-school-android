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

package app.ss.media.playback.ui.video.player

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import app.ss.media.R
import app.ss.media.playback.players.SSVideoPlayer
import app.ss.media.playback.players.VideoPlaybackState
import app.ss.media.playback.players.isBuffering
import app.ss.media.playback.ui.common.rememberFlowWithLifecycle
import app.ss.media.playback.ui.nowPlaying.components.PlayBackControlsDefaults
import com.cryart.design.theme.SSTheme

@Composable
fun VideoPlayerControls(
    videoPlayer: SSVideoPlayer,
    onClose: () -> Unit = {},
) {
    SSTheme {
        val playbackState by rememberFlowWithLifecycle(videoPlayer.playbackState)
            .collectAsState(initial = VideoPlaybackState())

        Surface(color = Color.Black.copy(0.6f)) {
            Box(modifier = Modifier.fillMaxSize()) {

                TopBar(onClose = onClose)

                Controls(
                    onPlayPause = {
                        videoPlayer.playPause()
                    },
                    onRewind = {
                        videoPlayer.rewind()
                    },
                    onForward = {
                        videoPlayer.fastForward()
                    },
                    playbackState = playbackState
                )
            }
        }
    }
}

@Composable
private fun BoxScope.TopBar(
    onClose: () -> Unit,
    contentColor: Color = Color.White
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.align(Alignment.TopCenter)
    ) {
        IconButton(onClick = onClose) {
            Icon(
                Icons.Rounded.Close,
                contentDescription = "Close",
                tint = contentColor,
            )
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun BoxScope.Controls(
    onPlayPause: () -> Unit,
    onRewind: () -> Unit,
    onForward: () -> Unit,
    playbackState: VideoPlaybackState,
    contentColor: Color = Color.White
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.align(Alignment.Center)
    ) {

        IconButton(
            onClick = onRewind,
            modifier = Modifier.size(PlayBackControlsDefaults.nonPlayButtonSize)
        ) {
            Icon(
                painterResource(id = R.drawable.ic_audio_icon_backward),
                contentDescription = "Rewind",
                tint = contentColor,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.width(PlayBackControlsDefaults.playButtonHorizontalPadding))

        Box(
            modifier = Modifier.size(PlayBackControlsDefaults.playButtonSize),
            contentAlignment = Alignment.Center
        ) {

            if (playbackState.isBuffering) {
                CircularProgressIndicator()
            } else {
                IconButton(
                    onClick = onPlayPause,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    val painter = when {
                        playbackState.isPlaying -> painterResource(id = R.drawable.ic_audio_icon_pause)
                        else -> painterResource(id = R.drawable.ic_audio_icon_play)
                    }
                    Icon(
                        painter = painter,
                        modifier = Modifier.fillMaxSize(),
                        contentDescription = "Play/Pause",
                        tint = contentColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(PlayBackControlsDefaults.playButtonHorizontalPadding))

        IconButton(
            onClick = onForward,
            modifier = Modifier.size(PlayBackControlsDefaults.nonPlayButtonSize)
        ) {
            Icon(
                painterResource(id = R.drawable.ic_audio_icon_forward),
                contentDescription = "Forward",
                tint = contentColor,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
