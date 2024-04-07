/*
 * Copyright (c) 2024. Adventech <info@adventech.io>
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
import app.ss.tv.presentation.player.VideoPlayerScreen.Event
import app.ss.tv.presentation.player.VideoPlayerScreen.State
import app.ss.tv.presentation.player.components.VideoPlayerControlsSpec
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.components.ActivityComponent
import ss.libraries.media.api.SSMediaPlayer
import ss.libraries.media.model.NowPlaying
import ss.libraries.media.model.PlaybackProgressState
import ss.libraries.media.model.PlaybackState
import ss.libraries.media.model.SSMediaItem

class VideoPlayerPresenter @AssistedInject constructor(
    private val ambientModeHelper: AmbientModeHelper,
    private val mediaPlayer: SSMediaPlayer,
    @Assisted private val screen: VideoPlayerScreen,
) : Presenter<State> {

    @CircuitInject(VideoPlayerScreen::class, ActivityComponent::class)
    @AssistedFactory
    interface Factory {
        fun create(
            screen: VideoPlayerScreen,
        ): VideoPlayerPresenter
    }

    @Composable
    override fun present(): State {
        val isConnected by produceRetainedState(initialValue = false) {
            mediaPlayer.isConnected.collect { isConnected -> value = isConnected }
        }
        val playbackState by produceRetainedState(initialValue = PlaybackState()) {
            mediaPlayer.playbackState.collect { state -> value = state }
        }
        val playbackProgress by produceRetainedState(initialValue = PlaybackProgressState()) {
            mediaPlayer.playbackProgress
                .collect { progress -> value = progress }
        }
        val nowPlaying by produceRetainedState(initialValue = NowPlaying.NONE) {
            mediaPlayer.nowPlaying.collect { nowPlaying -> value = nowPlaying }
        }

        LaunchedEffect(playbackState.isPlaying) {
            if (playbackState.isPlaying) {
                ambientModeHelper.disable()
            } else {
                ambientModeHelper.enable()
            }
        }

        DisposableEffect(Unit) {
            onDispose {
                mediaPlayer.release()
            }
        }

        return if (isConnected) {
            State.Playing(
                isPlaying = playbackState.isPlaying,
                isBuffering = playbackState.isBuffering,
                controls = VideoPlayerControlsSpec(
                    progressState = playbackProgress,
                    title = nowPlaying.title,
                    artist = nowPlaying.artist
                )
            ) { event ->
                when (event) {
                    is Event.OnPlayerViewCreated -> mediaPlayer.playItem(
                        SSMediaItem.Video(screen.video),
                        event.playerView,
                    )

                    Event.OnPlayPause -> mediaPlayer.playPause()
                    is Event.OnSeek -> mediaPlayer.seekTo(event.position)
                }
            }
        } else State.Loading
    }
}
