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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.net.toUri
import app.ss.tv.presentation.player.VideoPlayerScreen.Event
import app.ss.tv.presentation.player.VideoPlayerScreen.State
import app.ss.tv.presentation.player.components.VideoPlayerControlsSpec
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import ss.libraries.media.api.SSVideoPlayer
import ss.libraries.media.model.PlaybackProgressState
import ss.libraries.media.model.VideoPlaybackState
import ss.libraries.media.model.isBuffering

class VideoPlayerPresenter @AssistedInject constructor(
    private val ambientModeHelper: AmbientModeHelper,
    private val videoPlayer: SSVideoPlayer,
    @Assisted private val screen: VideoPlayerScreen,
) : Presenter<State> {

    @AssistedFactory
    interface Factory {
        fun create(
            screen: VideoPlayerScreen,
        ): VideoPlayerPresenter
    }

    @Composable
    override fun present(): State {
        val playbackState by produceRetainedState(initialValue = VideoPlaybackState()) {
            videoPlayer.playbackState.collect { state -> value = state }
        }
        val playbackProgress by produceRetainedState(initialValue = PlaybackProgressState()) {
            videoPlayer.playbackProgress
                .collect { progress -> value = progress }
        }
        val video = screen.video

        LaunchedEffect(playbackState.isPlaying) {
            if (playbackState.isPlaying) {
                ambientModeHelper.disable()
            } else {
                ambientModeHelper.enable()
            }
        }

        DisposableEffect(Unit) {
            onDispose {
                videoPlayer.release()
            }
        }

        return State(
            controls = VideoPlayerControlsSpec(
                isPlaying = playbackState.isPlaying,
                isBuffering = playbackState.isBuffering,
                onPlayPauseToggle = videoPlayer::playPause,
                onSeek = videoPlayer::seekTo,
                progressState = playbackProgress,
                title = video.title,
                artist = video.artist
            )
        ) { event ->
            when (event) {
                is Event.OnPlayerViewCreated -> videoPlayer.playVideo(
                    video.src.toUri(), event.playerView
                )
            }
        }
    }
}
