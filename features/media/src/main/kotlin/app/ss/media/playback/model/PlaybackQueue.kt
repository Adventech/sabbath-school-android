package app.ss.media.playback.model

import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import android.support.v4.media.session.MediaControllerCompat
import app.ss.media.playback.extensions.currentIndex

data class PlaybackQueue(
    val list: List<String> = emptyList(),
    val audiosList: List<AudioFile> = emptyList(),
    val title: String? = null,
    val initialMediaId: String = "",
    val currentIndex: Int = 0,
) : List<AudioFile> by audiosList {

    val isValid = list.isNotEmpty() && audiosList.isNotEmpty() && currentIndex >= 0

    val currentAudio get() = get(currentIndex)
}

fun fromMediaController(mediaController: MediaControllerCompat) = PlaybackQueue(
    title = mediaController.queueTitle?.toString(),
    list = mediaController.queue.mapNotNull { it.description.mediaId },
    initialMediaId = mediaController.metadata?.getString(METADATA_KEY_MEDIA_ID) ?: "",
    currentIndex = mediaController.playbackState.currentIndex
)
