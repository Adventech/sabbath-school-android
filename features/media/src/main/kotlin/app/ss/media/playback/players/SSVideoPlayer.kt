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

package app.ss.media.playback.players

import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.support.v4.media.session.MediaSessionCompat
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import app.ss.media.playback.AudioFocusHelper
import app.ss.media.playback.AudioFocusHelperImpl
import app.ss.media.playback.PLAYBACK_PROGRESS_INTERVAL
import app.ss.media.playback.model.PlaybackProgressState
import app.ss.models.media.SSVideo
import com.cryart.sabbathschool.core.extensions.coroutines.flow.flowInterval
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.ui.StyledPlayerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

@Immutable
data class VideoPlaybackState(
    @Player.State val state: Int = Player.STATE_IDLE,
    val isPlaying: Boolean = false
)

val VideoPlaybackState.isBuffering: Boolean get() = state == Player.STATE_BUFFERING
val VideoPlaybackState.hasEnded: Boolean get() = state == Player.STATE_ENDED

interface SSVideoPlayer {
    val playbackState: StateFlow<VideoPlaybackState>
    val playbackProgress: StateFlow<PlaybackProgressState>
    fun playVideo(video: SSVideo, playerView: StyledPlayerView)
    fun playPause()
    fun seekTo(position: Long)
    fun fastForward()
    fun rewind()
    fun onPause()
    fun onResume()
    fun release()
}

internal class SSVideoPlayerImpl(
    private val context: Context,
    private val audioFocusHelper: AudioFocusHelper = AudioFocusHelperImpl(context),
    coroutineScope: CoroutineScope = ProcessLifecycleOwner.get().lifecycleScope,
) : SSVideoPlayer, Player.Listener, CoroutineScope by coroutineScope {

    private val exoPlayer: ExoPlayer by lazy {
        ExoPlayer.Builder(context).build().also { player ->
            player.playWhenReady = false
            player.addListener(this)
        }
    }

    override val playbackState = MutableStateFlow(VideoPlaybackState())

    private var playbackProgressInterval: Job = Job()
    override val playbackProgress = MutableStateFlow(PlaybackProgressState())

    init {
        val mediaSession = MediaSessionCompat(context, "ss-video-player")
        mediaSession.isActive = true

        val connector = MediaSessionConnector(mediaSession)
        connector.setPlayer(exoPlayer)

        audioFocusHelper.onAudioFocusGain {
            if (isAudioFocusGranted && !exoPlayer.isPlaying) {
                exoPlayer.play()
            } else {
                audioFocusHelper.setVolume(AudioManager.ADJUST_RAISE)
            }
            isAudioFocusGranted = false
        }
        audioFocusHelper.onAudioFocusLoss {
            abandonPlayback()
            isAudioFocusGranted = false
            exoPlayer.pause()
        }

        audioFocusHelper.onAudioFocusLossTransient {
            if (exoPlayer.isPlaying) {
                isAudioFocusGranted = true
                exoPlayer.pause()
            }
        }

        audioFocusHelper.onAudioFocusLossTransientCanDuck {
            audioFocusHelper.setVolume(AudioManager.ADJUST_LOWER)
        }

        startPlaybackProgress()
    }

    override fun playVideo(video: SSVideo, playerView: StyledPlayerView) {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        }

        val mediaSource = DefaultMediaSourceFactory(context)
            .createMediaSource(MediaItem.fromUri(Uri.parse(video.src)))
        playerView.player = exoPlayer
        exoPlayer.setMediaSource(mediaSource)
        exoPlayer.prepare()
    }

    override fun playPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            if (playbackState.value.hasEnded) {
                exoPlayer.seekTo(0)
            }
            playOnFocus()
        }
    }

    override fun seekTo(position: Long) {
        exoPlayer.seekTo(position)
    }

    override fun fastForward() {
        val forwardTo = exoPlayer.currentPosition + DEFAULT_FORWARD
        if (forwardTo > exoPlayer.duration) {
            seekTo(exoPlayer.duration)
        } else {
            seekTo(forwardTo)
        }
    }

    override fun rewind() {
        val rewindTo = exoPlayer.currentPosition - DEFAULT_REWIND
        if (rewindTo < 0) {
            seekTo(0)
        } else {
            seekTo(rewindTo)
        }
    }

    override fun onPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        }
    }

    override fun onResume() {
        val state = playbackState.value
        if (exoPlayer.isPlaying.not() && state.state == Player.STATE_READY) {
            playOnFocus()
        }
    }

    private fun playOnFocus() {
        if (audioFocusHelper.requestPlayback()) {
            exoPlayer.play()
        }
    }

    override fun release() {
        exoPlayer.release()
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        Timber.e(error)
    }

    override fun onPlaybackStateChanged(state: Int) {
        super.onPlaybackStateChanged(state)
        playbackState.value = playbackState.value.copy(
            state = state
        )

        if (state == Player.STATE_READY) {
            playOnFocus()
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        super.onIsPlayingChanged(isPlaying)
        playbackState.value = playbackState.value.copy(
            isPlaying = isPlaying
        )
    }

    private fun startPlaybackProgress() = launch {
        playbackState.collect { state ->
            playbackProgressInterval.cancel()

            val duration = exoPlayer.duration
            val position = exoPlayer.currentPosition

            if (state.state == Player.STATE_IDLE || duration < 1)
                return@collect

            val initial = PlaybackProgressState(duration, position, buffered = exoPlayer.bufferedPosition)
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
                playbackProgress.value = initial.copy(elapsed = elapsed, buffered = exoPlayer.bufferedPosition)
            }
        }
    }
}
