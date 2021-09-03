package app.ss.media.playback

import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ss.media.repository.SSMediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaybackViewModel @Inject constructor(
    val playbackConnection: PlaybackConnection,
    private val repository: SSMediaRepository,
) : ViewModel() {

    init {
        viewModelScope.launch {
            playbackConnection.isConnected.collect { connected ->
                if (connected) {
                    playbackConnection.transportControls?.sendCustomAction(SET_MEDIA_STATE, null)
                }
            }
        }
    }

    fun playPause() {
        if (!playbackConnection.isConnected.value) return

        when (val state = playbackConnection.playbackState.value.state) {
            PlaybackStateCompat.STATE_PLAYING -> {
                playbackConnection.transportControls?.pause()
            }
            PlaybackStateCompat.STATE_PAUSED -> {
                playbackConnection.transportControls?.play()
            }
            else -> {
                if (state != PlaybackStateCompat.STATE_BUFFERING) {
                    viewModelScope.launch {
                        // sample id
                        val audio = repository.findAudioFile("876d52b6d4193883a43dba72ecd4d5d4c0b775b24fded652f3b667e1dfb0066e") ?: return@launch
                        playbackConnection.playAudio(audio)
                    }
                }
            }
        }
    }
}
