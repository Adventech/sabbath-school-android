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

package app.ss.media.playback.ui.nowPlaying

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ss.lessons.data.repository.media.MediaRepository
import app.ss.models.media.AudioFile
import com.cryart.sabbathschool.core.extensions.intent.lessonIndex
import com.cryart.sabbathschool.core.extensions.intent.readIndex
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ss.foundation.coroutines.flow.stateIn
import ss.libraries.media.model.extensions.id
import ss.libraries.media.model.extensions.targetIndex
import ss.libraries.media.model.toAudio
import ss.services.media.ui.PlaybackConnection
import javax.inject.Inject

@HiltViewModel
internal class NowPlayingViewModel @Inject constructor(
    private val repository: MediaRepository,
    val playbackConnection: PlaybackConnection,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val nowPlayingAudio: StateFlow<AudioFile> = playbackConnection.nowPlaying
        .map { metaData ->
            val def = metaData.toAudio()
            if (metaData.id.isEmpty()) {
                def
            } else {
                repository.findAudioFile(metaData.id) ?: def
            }
        }
        .stateIn(viewModelScope, AudioFile(""))

    init {
        viewModelScope.launch {
            playbackConnection.isConnected.collect { connected ->
                if (connected) {
                    generateQueue()
                }
            }
        }
    }

    private fun generateQueue() = viewModelScope.launch {
        val nowPlaying = playbackConnection.nowPlaying.first()
        val lessonIndex = savedStateHandle.lessonIndex ?: return@launch
        // Correct queue or playlist already set
        if (nowPlaying.targetIndex?.contains(lessonIndex) == true) return@launch

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

        val index = playlist.indexOfFirst { it.targetIndex == savedStateHandle.readIndex }
        val position = index.coerceAtLeast(0)
        playbackConnection.setQueue(playlist, index)

        if (play) {
            playbackConnection.playAudios(playlist, position)
        }
    }
}
