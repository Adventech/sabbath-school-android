package app.ss.media.playback

import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ss.media.playback.repository.AudioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaybackViewModel @Inject constructor(
    val playbackConnection: PlaybackConnection,
    private val repository: AudioRepository,
    private val savedStateHandle: SavedStateHandle
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
                        val audio = repository.findAudioFile("") ?: return@launch
                        playbackConnection.playAudio(audio)
                    }
                }
            }
        }
    }
}

private const val ARG_FILE_ID = "arg:file"
private val SavedStateHandle.fileId: String? get() = get(ARG_FILE_ID)
