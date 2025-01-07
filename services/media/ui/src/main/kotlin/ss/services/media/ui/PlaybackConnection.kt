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

package ss.services.media.ui

import androidx.compose.runtime.Stable
import androidx.media3.common.MediaMetadata
import app.ss.models.media.AudioFile
import kotlinx.coroutines.flow.StateFlow
import ss.libraries.media.model.PlaybackProgressState
import ss.libraries.media.model.PlaybackQueue
import ss.libraries.media.model.PlaybackSpeed
import ss.services.media.ui.spec.PlaybackStateSpec

@Stable
interface PlaybackConnection {
    val isConnected: StateFlow<Boolean>
    val playbackState: StateFlow<PlaybackStateSpec>
    val nowPlaying: StateFlow<MediaMetadata>

    val playbackQueue: StateFlow<PlaybackQueue>

    val playbackProgress: StateFlow<PlaybackProgressState>
    val playbackSpeed: StateFlow<PlaybackSpeed>

    fun playPause()
    fun playAudio(audio: AudioFile)
    fun playAudios(audios: List<AudioFile>, index: Int = 0)

    fun toggleSpeed()
    fun setQueue(audios: List<AudioFile>, index: Int = 0)
    fun skipToItem(position: Int)
    fun seekTo(progress: Long)
    fun rewind()
    fun fastForward()
    fun stop()
    fun releaseMini()
}
