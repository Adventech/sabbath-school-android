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

package ss.libraries.media.test

import androidx.media3.common.MediaMetadata
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ss.libraries.media.api.SSMediaPlayer
import ss.libraries.media.model.PlaybackProgressState
import ss.libraries.media.model.PlaybackSpeed
import ss.libraries.media.model.PlaybackState
import ss.libraries.media.model.SSMediaItem
import ss.libraries.media.model.extensions.NONE_PLAYING

/**
 * A fake implementation of [SSMediaPlayer] that can be used for testing.
 */
class FakeSSMediaPlayer(
    override val playbackState: StateFlow<PlaybackState> = MutableStateFlow(PlaybackState()),
    override val playbackProgress: StateFlow<PlaybackProgressState> = MutableStateFlow(PlaybackProgressState()),
    override val playbackSpeed: StateFlow<PlaybackSpeed> = MutableStateFlow(PlaybackSpeed.NORMAL),
    override val isConnected: StateFlow<Boolean> = MutableStateFlow(false),
    override val nowPlaying: StateFlow<MediaMetadata> = MutableStateFlow(NONE_PLAYING),
) : SSMediaPlayer {

    var serviceClass: Class<*>? = null
        private set
    var mediaItems: List<SSMediaItem>? = null
        private set
    var playerView: PlayerView? = null
        private set
    var seekTo: Long? = null
        private set
    var playPauseInvoked: Boolean = false
        private set

    override fun connect(service: Class<*>) {
        this.serviceClass = service
    }

    override fun playItem(mediaItem: SSMediaItem) {
        this.mediaItems = listOf(mediaItem)
    }

    override fun playItem(mediaItem: SSMediaItem, playerView: PlayerView) {
        this.mediaItems = listOf(mediaItem)
        this.playerView = playerView
    }

    override fun playItems(mediaItems: List<SSMediaItem>, index: Int) {
        this.mediaItems = mediaItems
    }

    override fun playPause() {
        playPauseInvoked = true
    }

    override fun seekTo(position: Long) {
        seekTo = position
    }

    override fun skipToItem(position: Int) = Unit

    override fun fastForward() = Unit

    override fun rewind() = Unit

    override fun toggleSpeed() = Unit

    override fun onPause() = Unit

    override fun onResume() = Unit

    override fun release() = Unit

}
