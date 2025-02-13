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

package app.ss.media.playback.ui.nowPlaying

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import app.ss.lessons.data.repository.media.MediaRepository
import app.ss.models.media.AudioFile
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import ss.libraries.circuit.navigation.AudioPlayerScreen
import ss.libraries.media.model.extensions.id
import ss.libraries.media.model.extensions.targetIndex
import ss.libraries.media.model.toAudio
import ss.services.media.ui.PlaybackConnection

class AudioPlayerPresenter @AssistedInject constructor(
    @Assisted private val screen: AudioPlayerScreen,
    private val repository: MediaRepository,
    private val playbackConnection: PlaybackConnection,
) : Presenter<AudioPlayerState> {

    @Composable
    override fun present(): AudioPlayerState {
        val isConnected by rememberIsConnected()
        val nowPlayingAudio by rememberNowPlayingAudio()
        val screenSpec by rememberScreenSpec(nowPlayingAudio)

        LaunchedEffect(isConnected) {
            if (isConnected) {
                generateQueue()
            }
        }

        val spec = screenSpec

        return when {
            spec == null -> AudioPlayerState.Loading
            else -> AudioPlayerState.NowPlaying(spec)
        }
    }

    @Composable
    private fun rememberIsConnected() = produceRetainedState(false) {
        playbackConnection.isConnected.collect { value = it }
    }

    @Composable
    private fun rememberNowPlayingAudio() = produceRetainedState(AudioFile("")) {
        playbackConnection.nowPlaying
            .map { metaData ->
                val def = metaData.toAudio()
                if (metaData.id.isEmpty()) {
                    def
                } else {
                    repository.findAudioFile(metaData.id) ?: def
                }
            }.collect { value = it }
    }

    @Composable
    private fun rememberScreenSpec(audioFile: AudioFile) = produceRetainedState<NowPlayingScreenSpec?>(null, audioFile) {
        combine(
            playbackConnection.playbackQueue,
            playbackConnection.playbackState,
            playbackConnection.playbackProgress,
            playbackConnection.playbackSpeed
        ) { queue, state, progress, speed ->
            NowPlayingScreenSpec(
                nowPlayingAudio = audioFile,
                playbackQueue = queue,
                playbackState = state,
                playbackProgressState = progress,
                playbackConnection = playbackConnection,
                playbackSpeed = speed,
                isDraggable = { isDraggable -> }
            )
        }.collect { value = it }
    }

    private suspend fun generateQueue() {
        val nowPlaying = playbackConnection.nowPlaying.first()
        val lessonIndex = screen.resourceId
        // Correct queue or playlist already set
        if (nowPlaying.targetIndex?.contains(lessonIndex) == true) return

        val playlist = repository.getPlayList(lessonIndex)

        if (nowPlaying.id.isEmpty()) {
            setAudioQueue(playlist)
        } else {
            val audio = repository.findAudioFile(nowPlaying.id)
            if (audio?.targetIndex?.startsWith(lessonIndex) == false) {
                val state = playbackConnection.playbackState.first()
                setAudioQueue(playlist, state.isPlaying)
            }
        }
    }

    private fun setAudioQueue(playlist: List<AudioFile>, play: Boolean = false) {
        if (playlist.isEmpty()) return

        val index = playlist.indexOfFirst { it.targetIndex == screen.documentIndex }
        val position = index.coerceAtLeast(0)
        playbackConnection.setQueue(playlist, index)

        if (play) {
            playbackConnection.playAudios(playlist, position)
        }
    }

    @CircuitInject(AudioPlayerScreen::class, SingletonComponent::class)
    @AssistedFactory
    interface Factory {
        fun create(screen: AudioPlayerScreen): AudioPlayerPresenter
    }
}
