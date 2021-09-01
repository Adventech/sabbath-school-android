package app.ss.media.playback

import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ss.media.playback.model.AudioFile
import app.ss.media.playback.model.isUnKnown
import app.ss.media.playback.model.toAudio
import app.ss.media.playback.repository.AudioRepository
import com.cryart.sabbathschool.core.extensions.coroutines.flow.stateIn
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@HiltViewModel
class PlaybackViewModel @Inject constructor(
    private val playbackConnection: PlaybackConnection,
    private val repository: AudioRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val playingAudioFlow: StateFlow<AudioFile?> = playbackConnection.nowPlaying.map { metadataCompat ->
        val audio = metadataCompat.toAudio()
        if (audio.isUnKnown) {
            savedStateHandle.fileId?.let { repository.findAudioFile(it) }
        } else {
            audio
        }
    }.stateIn(
        scope = viewModelScope,
        initialValue = null
    )

    val playBackFlow: Flow<PlaybackStateCompat> = playbackConnection.playbackState

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
