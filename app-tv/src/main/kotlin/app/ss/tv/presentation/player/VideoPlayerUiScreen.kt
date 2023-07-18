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

package app.ss.tv.presentation.player

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import app.ss.tv.presentation.extentions.handleDPadKeyEvents
import app.ss.tv.presentation.player.VideoPlayerScreen.Event
import app.ss.tv.presentation.player.VideoPlayerScreen.State
import app.ss.tv.presentation.player.components.VideoPlayerControls
import app.ss.tv.presentation.player.components.VideoPlayerControlsSpec
import kotlinx.coroutines.delay

@SuppressLint("OpaqueUnitKey")
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun VideoPlayerUiScreen(
    state: State,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var contentCurrentPosition by remember { mutableLongStateOf(0L) }
    val videoPlayerState = rememberVideoPlayerState()

    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .build()
            .apply {
                playWhenReady = true
                videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
                repeatMode = Player.REPEAT_MODE_ONE

                val source = ProgressiveMediaSource.Factory(DefaultDataSource.Factory(context))
                    .createMediaSource(MediaItem.fromUri(state.spec.src))
                setMediaSource(source)
                prepare()
            }
    }

    BackHandler(onBack = { state.eventSink(Event.OnBack) })

    LaunchedEffect(Unit) {
        while (true) {
            delay(300)
            contentCurrentPosition = exoPlayer.currentPosition
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        DisposableEffect(
            AndroidView(
                modifier = Modifier
                    .handleDPadKeyEvents(
                        onEnter = {
                            if (!videoPlayerState.isDisplayed) {
                                videoPlayerState.showControls()
                            }
                        }
                    )
                    .focusable(),
                factory = {
                    PlayerView(context).apply {
                        hideController()
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                        player = exoPlayer
                        layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    }
                }
            ),
        ) {
            onDispose { exoPlayer.release() }
        }

        VideoPlayerControls(
            VideoPlayerControlsSpec(
                isPlaying = exoPlayer.isPlaying,
                isBuffering = exoPlayer.playbackState == Player.STATE_BUFFERING,
                onPlayPauseToggle = {
                    if (exoPlayer.isPlaying) {
                        exoPlayer.pause()
                    } else {
                        exoPlayer.play()
                    }
                },
                onSeek = { seekProgress ->
                    exoPlayer.seekTo(exoPlayer.duration.times(seekProgress).toLong())
                },
                contentProgressInMillis = contentCurrentPosition,
                contentDurationInMillis = exoPlayer.duration,
                title = state.spec.title,
                artist = state.spec.artist
            ),
            videoPlayerState = videoPlayerState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }

    LaunchedEffect(exoPlayer.isPlaying) {
        state.eventSink(Event.OnPlaybackChange(exoPlayer.isPlaying))
    }
}
