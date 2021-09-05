package app.ss.media.playback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ss.media.playback.extensions.id
import app.ss.media.playback.extensions.isPlaying
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaybackViewModel @Inject constructor(
    val playbackConnection: PlaybackConnection,
) : ViewModel() {

    init {
        viewModelScope.launch {
            playbackConnection.isConnected.collect { connected ->
                if (connected) {
                    playbackConnection.transportControls?.sendCustomAction(SET_MEDIA_STATE, null)

                    val state = playbackConnection.playbackState.first()
                    val nowPlaying = playbackConnection.nowPlaying.first()
                    if (!state.isPlaying && nowPlaying.id.isNotEmpty()) {
                        playbackConnection.transportControls?.stop()
                    }
                }
            }
        }
    }
}
