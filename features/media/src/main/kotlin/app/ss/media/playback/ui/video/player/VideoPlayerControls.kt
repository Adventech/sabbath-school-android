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

package app.ss.media.playback.ui.video.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.ss.design.compose.widget.icon.IconBox
import app.ss.design.compose.widget.icon.IconButton
import app.ss.design.compose.widget.icon.IconSlot
import app.ss.design.compose.widget.icon.Icons
import app.ss.media.playback.ui.common.PlaybackSpeedLabel
import app.ss.media.playback.ui.nowPlaying.components.PlayBackControlsDefaults
import app.ss.media.playback.ui.nowPlaying.components.PlaybackProgressDuration
import ss.libraries.media.api.SSVideoPlayer
import ss.libraries.media.model.isBuffering
import app.ss.translations.R.string as RString
import ss.libraries.media.resources.R as MediaR

@Composable
internal fun VideoPlayerControls(
    videoPlayer: SSVideoPlayer,
    onClose: () -> Unit = {},
    onEnterPiP: (() -> Unit)? = null
) {

    Surface(color = Color.Black.copy(0.6f)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            TopBar(
                videoPlayer = videoPlayer,
                onClose = onClose,
                onEnterPiP = onEnterPiP,
            )

            Controls(
                videoPlayer = videoPlayer
            )

            PlayBackProgress(
                videoPlayer = videoPlayer
            )
        }
    }
}

@Composable
private fun BoxScope.TopBar(
    videoPlayer: SSVideoPlayer,
    onClose: () -> Unit,
    onEnterPiP: (() -> Unit)? = null,
    contentColor: Color = Color.White,
) {
    val playbackSpeed by videoPlayer.playbackSpeed.collectAsStateWithLifecycle()

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.align(Alignment.TopCenter)
    ) {
        IconButton(onClick = onClose) {
            IconBox(
                icon = Icons.Close,
                contentColor = contentColor
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        PlaybackSpeedLabel(
            playbackSpeed = playbackSpeed,
            toggleSpeed = { videoPlayer.toggleSpeed() },
            contentColor = Color.White
        )

        onEnterPiP?.let {
            IconButton(onClick = onEnterPiP) {
                IconBox(
                    icon = IconSlot.fromResource(
                        MediaR.drawable.ic_video_icon_pip,
                        contentDescription = RString.ss_picture_in_picture
                    ),
                    contentColor = contentColor
                )
            }
        }
    }
}

@Composable
private fun BoxScope.Controls(
    videoPlayer: SSVideoPlayer,
    contentColor: Color = Color.White
) {
    val playbackState by videoPlayer.playbackState.collectAsStateWithLifecycle()

    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.align(Alignment.Center)
    ) {
        IconButton(onClick = { videoPlayer.rewind() }) {
            IconBox(
                icon = IconSlot.fromResource(
                    MediaR.drawable.ic_audio_icon_backward,
                    contentDescription = RString.ss_action_rewind
                ),
                contentColor = contentColor,
                modifier = Modifier.size(PlayBackControlsDefaults.nonPlayButtonSize)
            )
        }

        Spacer(modifier = Modifier.width(PlayBackControlsDefaults.playButtonHorizontalPadding))

        Box(
            modifier = Modifier.size(PlayBackControlsDefaults.playButtonSize),
            contentAlignment = Alignment.Center
        ) {
            if (playbackState.isBuffering) {
                CircularProgressIndicator(color = contentColor)
            } else {
                IconButton(
                    onClick = { videoPlayer.playPause() },
                    modifier = Modifier.fillMaxSize()
                ) {
                    IconBox(
                        icon = IconSlot.fromResource(
                            res = when {
                                playbackState.isPlaying -> MediaR.drawable.ic_audio_icon_pause
                                else -> MediaR.drawable.ic_audio_icon_play
                            },
                            contentDescription = RString.ss_action_play_pause
                        ),
                        contentColor = contentColor,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(PlayBackControlsDefaults.playButtonHorizontalPadding))

        IconButton(onClick = { videoPlayer.fastForward() }) {
            IconBox(
                icon = IconSlot.fromResource(
                    MediaR.drawable.ic_audio_icon_forward,
                    contentDescription = RString.ss_action_forward
                ),
                contentColor = contentColor,
                modifier = Modifier.size(PlayBackControlsDefaults.nonPlayButtonSize)
            )
        }
    }
}

@Composable
private fun BoxScope.PlayBackProgress(
    videoPlayer: SSVideoPlayer,
) {
    val progressState by videoPlayer.playbackProgress.collectAsStateWithLifecycle()
    val playbackState by videoPlayer.playbackState.collectAsStateWithLifecycle()

    PlaybackProgressDuration(
        isBuffering = playbackState.isBuffering,
        progressState = progressState,
        onSeekTo = { videoPlayer.seekTo(it) },
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 32.dp),
        forceDark = true
    )
}
