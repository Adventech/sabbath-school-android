package app.ss.media.playback.players

import android.media.AudioManager
import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat.Builder
import android.support.v4.media.session.PlaybackStateCompat.STATE_NONE
import androidx.core.os.bundleOf
import app.ss.media.playback.AudioFocusHelper
import app.ss.media.playback.AudioQueueManager
import app.ss.media.playback.BY_UI_KEY
import app.ss.media.playback.PAUSE_ACTION
import app.ss.media.playback.PLAY_ACTION
import app.ss.media.playback.SET_MEDIA_STATE
import app.ss.media.playback.UPDATE_META_DATA
import app.ss.media.playback.UPDATE_QUEUE
import app.ss.media.playback.extensions.isPlaying
import app.ss.media.playback.model.toQueueItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

const val QUEUE_MEDIA_ID_KEY = "queue_media_id_key"
const val QUEUE_LIST_KEY = "queue_list_key"

class MediaSessionCallback(
    private val mediaSession: MediaSessionCompat,
    private val audioPlayer: SSAudioPlayer,
    private val audioFocusHelper: AudioFocusHelper,
    private val audioQueueManager: AudioQueueManager
) : MediaSessionCompat.Callback(), CoroutineScope by MainScope() {

    init {
        audioFocusHelper.onAudioFocusGain {
            if (isAudioFocusGranted && !audioPlayer.getSession().isPlaying()) {
                audioPlayer.playAudio()
            } else audioFocusHelper.setVolume(AudioManager.ADJUST_RAISE)
            isAudioFocusGranted = false
        }
        audioFocusHelper.onAudioFocusLoss {
            abandonPlayback()
            isAudioFocusGranted = false
            audioPlayer.pause()
        }

        audioFocusHelper.onAudioFocusLossTransient {
            if (audioPlayer.getSession().isPlaying()) {
                isAudioFocusGranted = true
                audioPlayer.pause()
            }
        }

        audioFocusHelper.onAudioFocusLossTransientCanDuck {
            audioFocusHelper.setVolume(AudioManager.ADJUST_LOWER)
        }
    }

    override fun onPause() {
        audioPlayer.pause()
    }

    override fun onPlay() {
        playOnFocus()
    }

    override fun onFastForward() {
        audioPlayer.fastForward()
    }

    override fun onRewind() {
        audioPlayer.rewind()
    }

    override fun onPlayFromMediaId(mediaId: String, extras: Bundle?) {
        launch { audioPlayer.setDataFromMediaId(mediaId, extras ?: bundleOf()) }
    }

    override fun onSeekTo(position: Long) {
        audioPlayer.seekTo(position)
    }

    override fun onSkipToNext() {
        launch { audioPlayer.nextAudio() }
    }

    override fun onSkipToPrevious() {
        launch { audioPlayer.previousAudio() }
    }

    override fun onSkipToQueueItem(id: Long) {
        launch { audioPlayer.skipTo(id.toInt()) }
    }

    override fun onStop() {
        audioPlayer.stop()
    }

    override fun onSetRepeatMode(repeatMode: Int) {
        super.onSetRepeatMode(repeatMode)
        val bundle = mediaSession.controller.playbackState.extras ?: Bundle()
        audioPlayer.setPlaybackState(
            Builder(mediaSession.controller.playbackState)
                .setExtras(
                    bundle.apply {
                        putInt(REPEAT_MODE, repeatMode)
                    }
                ).build()
        )
    }

    override fun onCustomAction(action: String?, extras: Bundle?) {
        when (action) {
            SET_MEDIA_STATE -> setSavedMediaSessionState()
            PAUSE_ACTION -> audioPlayer.pause(extras ?: bundleOf(BY_UI_KEY to true))
            PLAY_ACTION -> playOnFocus(extras ?: bundleOf(BY_UI_KEY to true))
            UPDATE_QUEUE -> {
                val audios = audioQueueManager.queue
                mediaSession.setQueue(
                    audios.mapIndexed { index, audio ->
                        audio.toQueueItem(index.toLong())
                    }
                )
            }
            UPDATE_META_DATA -> {
                audioPlayer.resetMedia()
            }
        }
    }

    private fun setSavedMediaSessionState() {
        val controller = mediaSession.controller ?: return
        if (controller.playbackState == null || controller.playbackState.state == STATE_NONE) {
            // audioPlayer.restoreQueueState()
        } else {
            restoreMediaSession()
        }
    }

    private fun restoreMediaSession() {
        mediaSession.setMetadata(mediaSession.controller.metadata)
        audioPlayer.setPlaybackState(mediaSession.controller.playbackState)
    }

    private fun playOnFocus(extras: Bundle = bundleOf(BY_UI_KEY to true)) {
        if (audioFocusHelper.requestPlayback()) {
            audioPlayer.playAudio(extras)
        }
    }
}
