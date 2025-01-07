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

package app.ss.media.playback

import android.content.ComponentName
import android.content.Context
import androidx.compose.runtime.Stable
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Player.STATE_BUFFERING
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import app.ss.media.playback.extensions.NONE_PLAYBACK_STATE
import app.ss.media.playback.ui.spec.PlaybackStateSpec
import app.ss.models.media.AudioFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import ss.foundation.coroutines.flow.flowInterval
import ss.libraries.media.api.PLAYBACK_PROGRESS_INTERVAL
import ss.libraries.media.model.PlaybackProgressState
import ss.libraries.media.model.PlaybackQueue
import ss.libraries.media.model.PlaybackSpeed
import ss.libraries.media.model.extensions.NONE_PLAYING
import ss.libraries.media.model.toMediaItem
import timber.log.Timber

private const val LOG_TAG = "PlaybackConnection"

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

internal class PlaybackConnectionImpl(
    private val context: Context,
    private val serviceComponent: ComponentName,
    coroutineScope: CoroutineScope = ProcessLifecycleOwner.get().lifecycleScope
) : PlaybackConnection, CoroutineScope by coroutineScope {

    override val isConnected = MutableStateFlow(false)
    override val playbackState = MutableStateFlow(PlaybackStateSpec.NONE)
    override val nowPlaying = MutableStateFlow(NONE_PLAYING)

    private val playbackQueueState = MutableStateFlow(PlaybackQueue())
    override val playbackQueue = playbackQueueState

    private var playbackProgressInterval: Job = Job()
    override val playbackProgress = MutableStateFlow(PlaybackProgressState())

    override val playbackSpeed = MutableStateFlow(PlaybackSpeed.NORMAL)

    private var mediaBrowser: MediaBrowser? = null

    private var currentProgressInterval: Long = PLAYBACK_PROGRESS_INTERVAL

    init {
        connect()
    }

    private fun connect() {
        launch {
            mediaBrowser = MediaBrowser.Builder(
                context,
                SessionToken(context, serviceComponent),
            )
                .buildAsync()
                .await()
                .apply {
                    addListener(PlayerListener())
                    shuffleModeEnabled = false
                }

            isConnected.update { true }

            startPlaybackProgress()
        }
    }

    override fun playPause() {
        mediaBrowser?.run {
            if (isPlaying) {
                pause()
            } else {
                play()
            }
            sessionActivity?.send()
        }
    }

    override fun playAudio(audio: AudioFile) = playAudios(audios = listOf(audio), index = 0)

    override fun playAudios(audios: List<AudioFile>, index: Int) {
        mediaBrowser?.run {
            setMediaItems(audios.map { it.toMediaItem() }, index, 0L)
            prepare()
            play()
            sessionActivity?.send()
        }
    }

    override fun toggleSpeed() {
        val nextSpeed = when (playbackSpeed.value) {
            PlaybackSpeed.SLOW -> PlaybackSpeed.NORMAL
            PlaybackSpeed.NORMAL -> PlaybackSpeed.FAST
            PlaybackSpeed.FAST -> PlaybackSpeed.FASTEST
            PlaybackSpeed.FASTEST -> PlaybackSpeed.SLOW
        }

        if (playbackSpeed.tryEmit(nextSpeed)) {
            mediaBrowser?.setPlaybackSpeed(nextSpeed.speed)
            resetPlaybackProgressInterval()
        }
    }

    override fun setQueue(audios: List<AudioFile>, index: Int) {
        val initialId = audios.getOrNull(index)?.id ?: ""
        playbackQueueState.update {
            PlaybackQueue(
                audiosList = audios,
                initialMediaId = initialId,
                currentIndex = index
            )
        }

        mediaBrowser?.run {
            setMediaItems(audios.map { it.toMediaItem() }, index, 0L)
            prepare()
        }
    }

    override fun skipToItem(position: Int) {
        mediaBrowser?.run {
            seekToDefaultPosition(position)
            sessionActivity?.send()
        }
    }

    override fun seekTo(progress: Long) {
        mediaBrowser?.run {
            seekTo(progress)
            sessionActivity?.send()
        }
    }

    override fun fastForward() {
        mediaBrowser?.run {
            seekForward()
            sessionActivity?.send()
        }
    }

    override fun rewind() {
        mediaBrowser?.run {
            seekBack()
            sessionActivity?.send()
        }
    }

    override fun stop() {
        mediaBrowser?.run {
            stop()
            sessionActivity?.send()
        }
    }

    override fun releaseMini() {
        mediaBrowser?.run {
            pause()
            sessionActivity?.send()
        }
        playbackState.update { it.copy(canShowMini = false) }
    }

    private fun startPlaybackProgress() = launch {
        combine(playbackState, nowPlaying, ::Pair).collect { (state, current) ->
            playbackProgressInterval.cancel()
            val duration = mediaBrowser?.duration ?: return@collect
            val position = mediaBrowser?.currentPosition ?: return@collect
            val bufferedPosition = mediaBrowser?.bufferedPosition ?: return@collect

            if (state == NONE_PLAYBACK_STATE || current == NONE_PLAYING || duration < 1) {
                return@collect
            }

            val initial = PlaybackProgressState(duration, position, buffered = bufferedPosition)
            playbackProgress.emit(initial)

            if (state.isPlaying && !state.isBuffering) {
                startPlaybackProgressInterval(initial)
            }
        }
    }

    private fun startPlaybackProgressInterval(initial: PlaybackProgressState) {
        playbackProgressInterval = launch {
            flowInterval(currentProgressInterval).collect {
                val current = playbackProgress.value.elapsed
                val elapsed = current + PLAYBACK_PROGRESS_INTERVAL
                val bufferedPosition = mediaBrowser?.bufferedPosition ?: return@collect
                playbackProgress.update {
                    initial.copy(
                        elapsed = elapsed,
                        buffered = bufferedPosition,
                    )
                }
            }
        }
    }

    private fun resetPlaybackProgressInterval() {
        val speed = playbackSpeed.value.speed
        currentProgressInterval = (PLAYBACK_PROGRESS_INTERVAL.toDouble() / speed).toLong()

        playbackProgressInterval.cancel()
        startPlaybackProgressInterval(playbackProgress.value)
    }

    private inner class PlayerListener : Player.Listener {
        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            super.onMediaMetadataChanged(mediaMetadata)
            Timber.tag(LOG_TAG).i("onMediaMetadataChanged: $mediaMetadata")
            nowPlaying.update { mediaMetadata }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            Timber.i("onIsPlayingChanged: $isPlaying")
            playbackState.update {
                it.copy(
                    isPlaying = isPlaying,
                    isPlayEnabled = true,
                    canShowMini = if (isPlaying) true else it.canShowMini
                )
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            Timber.i("onPlaybackStateChanged: $playbackState")
            this@PlaybackConnectionImpl.playbackState.update {
                it.copy(isBuffering = playbackState == STATE_BUFFERING)
            }
        }

        override fun onIsLoadingChanged(isLoading: Boolean) {
            super.onIsLoadingChanged(isLoading)
            playbackState.update { it.copy(isBuffering = isLoading) }
        }

        override fun onPlayerErrorChanged(error: PlaybackException?) {
            super.onPlayerErrorChanged(error)
            Timber.e("onPlayerErrorChanged: $error")
            playbackState.update { it.copy(isError = error != null) }
        }

        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            Timber.e(error)
            playbackState.update { it.copy(isError = true) }
            isConnected.update { false }
        }
    }
}
