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

package app.ss.media.playback.ui.nowPlaying

import androidx.media3.common.MediaMetadata
import app.ss.media.playback.ui.nowPlaying.components.sampleAudio
import app.ss.models.media.AudioFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ss.libraries.media.model.PlaybackProgressState
import ss.libraries.media.model.PlaybackQueue
import ss.libraries.media.model.PlaybackSpeed
import ss.services.media.ui.PlaybackConnection
import ss.services.media.ui.spec.PlaybackStateSpec

internal object PreviewData {

    fun nowPlayScreenSpec(): NowPlayingScreenSpec {
        return NowPlayingScreenSpec(
            nowPlayingAudio = sampleAudio,
            playbackQueue = PlaybackQueue(
                audiosList = (1 until 10).map { sampleAudio.copy(id = "$it", title = "${sampleAudio.title} $it") } ,
                title = "Playback Queue"),
            playbackState = PlaybackStateSpec.NONE,
            playbackProgressState = PlaybackProgressState(),
            playbackConnection = object : PlaybackConnection {
                override val isConnected: StateFlow<Boolean>
                    get() = MutableStateFlow(true)
                override val playbackState: StateFlow<PlaybackStateSpec>
                    get() = MutableStateFlow(PlaybackStateSpec.NONE)
                override val nowPlaying: StateFlow<MediaMetadata>
                    get() = MutableStateFlow(MediaMetadata.Builder().build())
                override val playbackQueue: StateFlow<PlaybackQueue>
                    get() = MutableStateFlow(PlaybackQueue())
                override val playbackProgress: StateFlow<PlaybackProgressState>
                    get() = MutableStateFlow(PlaybackProgressState())
                override val playbackSpeed: StateFlow<PlaybackSpeed>
                    get() = MutableStateFlow(PlaybackSpeed.NORMAL)
                override fun playPause() = Unit
                override fun playAudio(audio: AudioFile) = Unit
                override fun playAudios(audios: List<AudioFile>, index: Int) = Unit
                override fun toggleSpeed() = Unit
                override fun setQueue(audios: List<AudioFile>, index: Int) = Unit
                override fun skipToItem(position: Int) = Unit
                override fun seekTo(progress: Long) = Unit
                override fun rewind() = Unit
                override fun fastForward() = Unit
                override fun stop() = Unit
                override fun releaseMini() = Unit
            },
            playbackSpeed = PlaybackSpeed.NORMAL,
            isDraggable = { },
        )
    }
}
