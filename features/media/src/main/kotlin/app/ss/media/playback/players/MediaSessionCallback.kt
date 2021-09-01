package app.ss.media.playback.players

import android.media.AudioManager
import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat.Builder
import android.support.v4.media.session.PlaybackStateCompat.STATE_NONE
import androidx.core.os.bundleOf
import app.ss.media.playback.AudioFocusHelper
import app.ss.media.playback.BY_UI_KEY
import app.ss.media.playback.PAUSE_ACTION
import app.ss.media.playback.PLAY_ACTION
import app.ss.media.playback.SET_MEDIA_STATE
import app.ss.media.playback.extensions.isPlaying
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import timber.log.Timber

const val SEEK_TO = "action_seek_to"

const val QUEUE_MEDIA_ID_KEY = "queue_media_id_key"
const val QUEUE_TITLE_KEY = "queue_title_key"
const val QUEUE_LIST_KEY = "queue_list_key"

class MediaSessionCallback(
    private val mediaSession: MediaSessionCompat,
    private val audioPlayer: SSAudioPlayer,
    private val audioFocusHelper: AudioFocusHelper,
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
        Timber.d("onPause")
        audioPlayer.pause()
    }

    override fun onPlay() {
        Timber.d("onPlay")
        playOnFocus()
    }

    override fun onFastForward() {
        Timber.d("onFastForward")
        audioPlayer.fastForward()
    }

    override fun onRewind() {
        Timber.d("onRewind")
        audioPlayer.rewind()
    }

    override fun onPlayFromMediaId(mediaId: String, extras: Bundle?) {
        Timber.d("onPlayFromMediaId, $mediaId, ${extras?.readable()}")
        launch { audioPlayer.setDataFromMediaId(mediaId, extras ?: bundleOf()) }
    }

    override fun onSeekTo(position: Long) {
        Timber.d("onSeekTo: position=$position")
        audioPlayer.seekTo(position)
    }

    override fun onStop() {
        Timber.d("onStop()")
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
        }
    }

    private fun setSavedMediaSessionState() {
        val controller = mediaSession.controller ?: return
        Timber.d(controller.playbackState.toString())
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
        if (audioFocusHelper.requestPlayback())
            audioPlayer.playAudio(extras)
    }
}

@OptIn(ExperimentalStdlibApi::class)
fun Bundle.readable() = buildList {
    keySet().forEach {
        add("key=$it, value=${get(it)}")
    }
}.joinToString()
