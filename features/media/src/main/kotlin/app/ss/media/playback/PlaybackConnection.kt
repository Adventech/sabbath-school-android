/*
 * Copyright (c) 2021. Adventech <info@adventech.io>
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
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package app.ss.media.playback

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import app.ss.media.playback.extensions.NONE_PLAYBACK_STATE
import app.ss.media.playback.extensions.NONE_PLAYING
import app.ss.media.playback.extensions.duration
import app.ss.media.playback.extensions.isBuffering
import app.ss.media.playback.extensions.isPlaying
import app.ss.media.playback.model.AudioFile
import app.ss.media.playback.model.MEDIA_TYPE_AUDIO
import app.ss.media.playback.model.MediaId
import app.ss.media.playback.model.PlaybackModeState
import app.ss.media.playback.model.PlaybackProgressState
import app.ss.media.playback.model.PlaybackQueue
import app.ss.media.playback.model.PlaybackSpeed
import app.ss.media.playback.players.AudioPlayer
import app.ss.media.playback.players.QUEUE_LIST_KEY
import com.cryart.sabbathschool.core.extensions.coroutines.flow.flowInterval
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

const val PLAYBACK_PROGRESS_INTERVAL = 1000L

interface PlaybackConnection {
    val isConnected: StateFlow<Boolean>
    val playbackState: StateFlow<PlaybackStateCompat>
    val nowPlaying: StateFlow<MediaMetadataCompat>

    val playbackQueue: SharedFlow<PlaybackQueue>

    val playbackProgress: StateFlow<PlaybackProgressState>
    val playbackMode: StateFlow<PlaybackModeState>
    val playbackSpeed: StateFlow<PlaybackSpeed>

    var mediaController: MediaControllerCompat?
    val transportControls: MediaControllerCompat.TransportControls?

    fun playAudio(audio: AudioFile)
    fun playAudios(audios: List<AudioFile>, index: Int = 0)

    fun toggleSpeed(playbackSpeed: PlaybackSpeed)
    fun setQueue(audios: List<AudioFile>, index: Int = 0)
}

internal class PlaybackConnectionImpl(
    context: Context,
    serviceComponent: ComponentName,
    private val audioPlayer: AudioPlayer,
    coroutineScope: CoroutineScope = ProcessLifecycleOwner.get().lifecycleScope,
) : PlaybackConnection, CoroutineScope by coroutineScope {

    override val isConnected = MutableStateFlow(false)
    override val playbackState = MutableStateFlow(NONE_PLAYBACK_STATE)
    override val nowPlaying = MutableStateFlow(NONE_PLAYING)

    private val playbackQueueState = MutableStateFlow(PlaybackQueue())
    override val playbackQueue = playbackQueueState

    private var playbackProgressInterval: Job = Job()
    override val playbackProgress = MutableStateFlow(PlaybackProgressState())

    override val playbackMode = MutableStateFlow(PlaybackModeState())
    override val playbackSpeed = MutableStateFlow(PlaybackSpeed.NORMAL)

    override var mediaController: MediaControllerCompat? = null
    override val transportControls get() = mediaController?.transportControls

    private val mediaBrowserConnectionCallback = MediaBrowserConnectionCallback(context)
    private val mediaBrowser = MediaBrowserCompat(
        context, serviceComponent, mediaBrowserConnectionCallback, null
    ).apply { connect() }

    init {
        startPlaybackProgress()
    }

    override fun playAudio(audio: AudioFile) = playAudios(audios = listOf(audio), index = 0)

    override fun playAudios(audios: List<AudioFile>, index: Int) {
        val audiosIds = audios.map { it.id }.toTypedArray()
        val audio = audios[index]
        transportControls?.playFromMediaId(
            MediaId(MEDIA_TYPE_AUDIO, audio.id).toString(),
            Bundle().apply {
                putStringArray(QUEUE_LIST_KEY, audiosIds)
            }
        )
        transportControls?.sendCustomAction(UPDATE_QUEUE, bundleOf())
    }

    override fun toggleSpeed(playbackSpeed: PlaybackSpeed) {
        val nextSpeed = when (playbackSpeed) {
            PlaybackSpeed.SLOW -> PlaybackSpeed.NORMAL
            PlaybackSpeed.NORMAL -> PlaybackSpeed.FAST
            PlaybackSpeed.FAST -> PlaybackSpeed.FASTEST
            PlaybackSpeed.FASTEST -> PlaybackSpeed.SLOW
        }
        audioPlayer.setPlaybackSpeed(nextSpeed.speed)
        this.playbackSpeed.value = nextSpeed
    }

    override fun setQueue(audios: List<AudioFile>, index: Int) {
        val audiosIds = audios.map { it.id }
        val initialId = audios.getOrNull(index)?.id ?: ""
        val playbackQueue = PlaybackQueue(
            list = audiosIds,
            audiosList = audios,
            initialMediaId = initialId,
            currentIndex = index
        )
        this.playbackQueueState.value = playbackQueue
    }

    private fun startPlaybackProgress() = launch {
        combine(playbackState, nowPlaying, ::Pair).collect { (state, current) ->
            playbackProgressInterval.cancel()
            val duration = current.duration
            val position = state.position

            if (state == NONE_PLAYBACK_STATE || current == NONE_PLAYING || duration < 1)
                return@collect

            val initial = PlaybackProgressState(duration, position, buffered = audioPlayer.bufferedPosition())
            playbackProgress.value = initial

            if (state.isPlaying && !state.isBuffering) {
                starPlaybackProgressInterval(initial)
            }
        }
    }

    private fun starPlaybackProgressInterval(initial: PlaybackProgressState) {
        playbackProgressInterval = launch {
            flowInterval(PLAYBACK_PROGRESS_INTERVAL).collect { ticks ->
                val elapsed = PLAYBACK_PROGRESS_INTERVAL * (ticks + 1)
                playbackProgress.value = initial.copy(elapsed = elapsed, buffered = audioPlayer.bufferedPosition())
            }
        }
    }

    private inner class MediaBrowserConnectionCallback(private val context: Context) :
        MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken).apply {
                registerCallback(MediaControllerCallback())
            }

            isConnected.value = true
        }

        override fun onConnectionSuspended() {
            isConnected.value = false
        }

        override fun onConnectionFailed() {
            isConnected.value = false
        }
    }

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            playbackState.value = state ?: return
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            nowPlaying.value = metadata ?: return
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            playbackMode.value = playbackMode.value.copy(repeatMode = repeatMode)
        }

        override fun onShuffleModeChanged(shuffleMode: Int) {
            playbackMode.value = playbackMode.value.copy(shuffleMode = shuffleMode)
        }

        override fun onSessionDestroyed() {
            mediaBrowserConnectionCallback.onConnectionSuspended()
        }
    }
}
