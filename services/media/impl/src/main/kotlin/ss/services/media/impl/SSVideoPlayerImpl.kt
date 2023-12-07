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

package ss.services.media.impl

import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.ui.PlayerView
import app.ss.models.media.SSVideo
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import ss.foundation.coroutines.flow.flowInterval
import ss.libraries.media.api.PLAYBACK_PROGRESS_INTERVAL
import ss.libraries.media.api.SSVideoPlayer
import ss.libraries.media.model.PlaybackProgressState
import ss.libraries.media.model.PlaybackSpeed
import ss.libraries.media.model.VideoPlaybackState
import ss.libraries.media.model.extensions.NONE_PLAYING
import ss.libraries.media.model.isBuffering
import ss.libraries.media.model.toMediaItem
import timber.log.Timber
import javax.inject.Inject

private const val LOG_TAG = "SSVideoPlayerImpl"

@ActivityScoped
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
internal class SSVideoPlayerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : SSVideoPlayer, Player.Listener, CoroutineScope by ProcessLifecycleOwner.get().lifecycleScope {

    override val isConnected = MutableStateFlow(false)
    override val playbackState = MutableStateFlow(VideoPlaybackState())
    override val nowPlaying = MutableStateFlow(NONE_PLAYING)
    override val playbackProgress = MutableStateFlow(PlaybackProgressState())
    override val playbackSpeed = MutableStateFlow(PlaybackSpeed.NORMAL)

    private var playbackProgressInterval: Job = Job()

    private var currentProgressInterval: Long = PLAYBACK_PROGRESS_INTERVAL

    private var mediaController: MediaController? = null

    override fun connect(service: Class<*>) {
        if (mediaController?.isConnected == true) {
            return
        }
        launch {
            mediaController = MediaController.Builder(
                context,
                SessionToken(context, ComponentName(context, service)),
            )
                .buildAsync()
                .await().apply {
                    addListener(this@SSVideoPlayerImpl)
                }

            isConnected.update { true }

            startPlaybackProgress()
        }
    }

    override fun playVideo(video: SSVideo, playerView: PlayerView) {
        mediaController?.run {
            if (isPlaying) {
                pause()
            }

            playerView.player = mediaController
            setMediaItem(video.toMediaItem())
            prepare()
        }
    }

    override fun playPause() {
        mediaController?.run {
            if (isPlaying) {
                pause()
            } else {
                if (playbackState == Player.STATE_ENDED) {
                    seekTo(0)
                }
                play()
            }
        }
    }

    override fun seekTo(position: Long) {
        mediaController?.seekTo(position)
    }

    override fun fastForward() {
        mediaController?.seekForward()
    }

    override fun rewind() {
        mediaController?.seekBack()
    }

    override fun toggleSpeed() {
        val nextSpeed = when (this.playbackSpeed.value) {
            PlaybackSpeed.SLOW -> PlaybackSpeed.NORMAL
            PlaybackSpeed.NORMAL -> PlaybackSpeed.FAST
            PlaybackSpeed.FAST -> PlaybackSpeed.FASTEST
            PlaybackSpeed.FASTEST -> PlaybackSpeed.SLOW
        }

        if (playbackSpeed.tryEmit(nextSpeed)) {
            mediaController?.setPlaybackSpeed(nextSpeed.speed)
            resetPlaybackProgressInterval()
        }
    }

    override fun onPause() {
        mediaController?.run {
            if (isPlaying) {
                pause()
            }
        }
    }

    override fun onResume() {
        mediaController?.run {
            if (isPlaying.not() && playbackState == Player.STATE_READY) {
                play()
            }
        }
    }

    override fun release() {
        mediaController?.release()
    }

    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
        super.onMediaMetadataChanged(mediaMetadata)
        Timber.tag(LOG_TAG).i("onMediaMetadataChanged: $mediaMetadata")
        nowPlaying.update { mediaMetadata }
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        Timber.e(error)
    }

    override fun onPlaybackStateChanged(state: Int) {
        super.onPlaybackStateChanged(state)
        Timber.tag(LOG_TAG).i("onPlaybackStateChanged: $state")
        playbackState.update {
            it.copy(state = state)
        }

        if (state == Player.STATE_READY) {
            mediaController?.play()
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        super.onIsPlayingChanged(isPlaying)
        playbackState.update {
            it.copy(isPlaying = isPlaying)
        }
    }

    private fun startPlaybackProgress() = launch {
        playbackState.collect { state ->
            playbackProgressInterval.cancel()

            val duration = mediaController?.duration ?: return@collect
            val position = mediaController?.currentPosition ?: return@collect
            val bufferedPosition = mediaController?.bufferedPosition ?: return@collect

            if (state.state == Player.STATE_IDLE || duration < 1) {
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
                val bufferedPosition = mediaController?.bufferedPosition ?: return@collect
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
}
