package app.ss.media.playback.players

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import app.ss.lessons.data.repository.media.MediaRepository
import app.ss.media.playback.AudioFocusHelper
import app.ss.media.playback.AudioQueueManager
import app.ss.media.playback.BY_UI_KEY
import app.ss.media.playback.REPEAT_ALL
import app.ss.media.playback.REPEAT_ONE
import app.ss.media.playback.SAFE_FLAG_IMMUTABLE
import app.ss.media.playback.extensions.createDefaultPlaybackState
import app.ss.media.playback.extensions.getBitmap
import app.ss.media.playback.extensions.isPlaying
import app.ss.media.playback.extensions.position
import app.ss.media.playback.extensions.repeatMode
import app.ss.media.playback.extensions.shuffleMode
import app.ss.media.playback.model.toMediaId
import app.ss.media.playback.model.toMediaMetadata
import app.ss.models.media.AudioFile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import app.ss.translations.R as L10n

typealias OnMetaDataChanged = SSAudioPlayer.() -> Unit

const val REPEAT_MODE = "repeat_mode"
const val SHUFFLE_MODE = "shuffle_mode"
const val QUEUE_CURRENT_INDEX = "queue_current_index"
const val QUEUE_HAS_PREVIOUS = "queue_has_previous"
const val QUEUE_HAS_NEXT = "queue_has_next"

internal const val DEFAULT_FORWARD = 30 * 1000
internal const val DEFAULT_REWIND = 15 * 1000
private const val COVER_IMAGE_SIZE = 300 // px

interface SSAudioPlayer {
    fun getSession(): MediaSessionCompat
    fun playAudio(extras: Bundle = bundleOf(BY_UI_KEY to true))
    suspend fun playAudio(id: String)
    suspend fun playAudio(audio: AudioFile)
    fun seekTo(position: Long)
    fun fastForward()
    fun rewind()
    fun pause(extras: Bundle = bundleOf(BY_UI_KEY to true))
    suspend fun nextAudio(): String?
    suspend fun previousAudio()
    suspend fun repeatAudio()
    fun stop(byUser: Boolean = true)
    suspend fun skipTo(position: Int)
    fun release()
    fun onPlayingState(playing: OnIsPlaying<SSAudioPlayer>)
    fun onPrepared(prepared: OnPrepared<SSAudioPlayer>)
    fun onError(error: OnError<SSAudioPlayer>)
    fun onCompletion(completion: OnCompletion<SSAudioPlayer>)
    fun onMetaDataChanged(metaDataChanged: OnMetaDataChanged)
    fun updatePlaybackState(applier: PlaybackStateCompat.Builder.() -> Unit = {})
    fun setPlaybackState(state: PlaybackStateCompat)
    suspend fun setDataFromMediaId(_mediaId: String, extras: Bundle = bundleOf())
    fun resetMedia()
}

@Singleton
internal class SSAudioPlayerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val audioPlayer: AudioPlayer,
    private val audioFocusHelper: AudioFocusHelper,
    private val queueManager: AudioQueueManager,
    private val repository: MediaRepository
) : SSAudioPlayer, CoroutineScope by MainScope() {

    private var isInitialized: Boolean = false

    private var isPlayingCallback: OnIsPlaying<SSAudioPlayer> = { _, _ -> }
    private var preparedCallback: OnPrepared<SSAudioPlayer> = {}
    private var errorCallback: OnError<SSAudioPlayer> = {}
    private var completionCallback: OnCompletion<SSAudioPlayer> = {}
    private var metaDataChangedCallback: OnMetaDataChanged = {}

    private val metadataBuilder = MediaMetadataCompat.Builder()
    private val stateBuilder = createDefaultPlaybackState()

    private val pendingIntent = PendingIntent.getBroadcast(context, 0, Intent(Intent.ACTION_MEDIA_BUTTON), SAFE_FLAG_IMMUTABLE)

    private val mediaSession = MediaSessionCompat(context, context.getString(L10n.string.ss_app_name), null, pendingIntent).apply {
        setCallback(
            MediaSessionCallback(
                this,
                this@SSAudioPlayerImpl,
                audioFocusHelper,
                queueManager
            )
        )
        setPlaybackState(stateBuilder.build())

        val sessionIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val sessionActivityPendingIntent = PendingIntent.getActivity(context, 0, sessionIntent, SAFE_FLAG_IMMUTABLE)
        setSessionActivity(sessionActivityPendingIntent)
        isActive = true
    }

    init {
        audioPlayer.onPrepared {
            preparedCallback(this@SSAudioPlayerImpl)
            launch {
                if (!mediaSession.isPlaying()) {
                    audioPlayer.seekTo(mediaSession.position())
                }
                playAudio()
            }
        }

        audioPlayer.onCompletion {
            completionCallback(this@SSAudioPlayerImpl)
            val controller = getSession().controller
            when (controller.repeatMode) {
                PlaybackStateCompat.REPEAT_MODE_ONE -> controller.transportControls.sendCustomAction(REPEAT_ONE, null)
                PlaybackStateCompat.REPEAT_MODE_ALL -> controller.transportControls.sendCustomAction(REPEAT_ALL, null)
                else -> launch { if (nextAudio() == null) goToStart() }
            }
        }
        audioPlayer.onBuffering {
            updatePlaybackState {
                setState(PlaybackStateCompat.STATE_BUFFERING, mediaSession.position(), 1F)
            }
        }
        audioPlayer.onIsPlaying { playing, byUi ->
            if (playing) {
                updatePlaybackState {
                    setState(PlaybackStateCompat.STATE_PLAYING, mediaSession.position(), 1F)
                    setExtras(
                        bundleOf(
                            REPEAT_MODE to getSession().repeatMode,
                            SHUFFLE_MODE to getSession().shuffleMode
                        )
                    )
                }
            }
            isPlayingCallback(playing, byUi)
        }
        audioPlayer.onReady {
            updateAudioDuration()

            if (!audioPlayer.isPlaying()) {
                Timber.d("Player ready but not currently playing, requesting to play")
                audioPlayer.play()
            }
            updatePlaybackState {
                setState(PlaybackStateCompat.STATE_PLAYING, mediaSession.position(), 1F)
            }
        }
        audioPlayer.onError { throwable ->
            Timber.e(throwable, "AudioPlayer error")
            errorCallback(this@SSAudioPlayerImpl, throwable)
            isInitialized = false
            updatePlaybackState {
                setState(PlaybackStateCompat.STATE_ERROR, 0, 1F)
            }
        }
    }

    private fun updateAudioDuration() {
        queueManager.currentAudio?.let { audio ->
            val duration = audioPlayer.duration()
            if (duration > 0 && audio.duration != duration) {
                launch {
                    repository.updateDuration(queueManager.currentAudioId, duration)
                    val updatedAudio = audio.copy(
                        duration = duration
                    )
                    queueManager.currentAudio = updatedAudio
                    setMetaData(updatedAudio)
                }
            }
        }
    }

    override fun getSession(): MediaSessionCompat = mediaSession

    override fun playAudio(extras: Bundle) {
        if (isInitialized) {
            audioPlayer.play()
            return
        }

        launch {
            repository.findAudioFile(queueManager.currentAudioId)?.let { audio ->
                queueManager.currentAudio = audio
                audioPlayer.setSource(audio.source, false)

                isInitialized = true
                audioPlayer.prepare()
            } ?: run {
                Timber.e("Couldn't set new source")
            }
        }
    }

    override suspend fun playAudio(id: String) {
        if (audioFocusHelper.requestPlayback()) {
            val audio = repository.findAudioFile(id) ?: run {
                Timber.e("Audio by id: $id not found")
                updatePlaybackState {
                    setState(PlaybackStateCompat.STATE_ERROR, 0, 1F)
                }
                return
            }
            queueManager.currentAudio = audio
            playAudio(audio)
        }
    }

    override suspend fun playAudio(audio: AudioFile) {
        queueManager.setCurrentAudioId(audio.id)
        queueManager.currentAudio = audio
        isInitialized = false

        updatePlaybackState {
            setState(mediaSession.controller.playbackState.state, 0, 1F)
        }
        setMetaData(audio)
        playAudio()
    }

    override fun seekTo(position: Long) {
        if (isInitialized) {
            audioPlayer.seekTo(position)
            updatePlaybackState {
                setState(
                    mediaSession.controller.playbackState.state,
                    position,
                    1F
                )
            }
        } else updatePlaybackState {
            setState(
                mediaSession.controller.playbackState.state,
                position,
                1F
            )
        }
    }

    override fun fastForward() {
        val forwardTo = mediaSession.position() + DEFAULT_FORWARD
        queueManager.currentAudio?.apply {
            if (forwardTo > duration) {
                seekTo(duration)
            } else {
                seekTo(forwardTo)
            }
        }
    }

    override fun rewind() {
        val rewindTo = mediaSession.position() - DEFAULT_REWIND
        if (rewindTo < 0) {
            seekTo(0)
        } else {
            seekTo(rewindTo)
        }
    }

    override fun pause(extras: Bundle) {
        if (isInitialized && (audioPlayer.isPlaying() || audioPlayer.isBuffering())) {
            audioPlayer.pause()
            updatePlaybackState {
                setState(PlaybackStateCompat.STATE_PAUSED, mediaSession.position(), 1F)
                setExtras(
                    extras + bundleOf(
                        REPEAT_MODE to getSession().repeatMode,
                        SHUFFLE_MODE to getSession().shuffleMode
                    )
                )
            }
        } else {
            Timber.d("Couldn't pause player: ${audioPlayer.isPlaying()}, $isInitialized")
        }
    }

    override suspend fun nextAudio(): String? {
        val index = queueManager.nextAudioIndex
        if (index != null) {
            val audio = queueManager.queue[index]
            playAudio(audio)
            return audio.id
        }
        return null
    }

    override suspend fun previousAudio() {
        if (queueManager.queue.isNotEmpty()) {
            queueManager.previousAudioIndex?.let {
                playAudio(queueManager.queue[it])
            } ?: repeatAudio()
        }
    }

    override suspend fun repeatAudio() {
        playAudio(queueManager.currentAudioId)
    }

    override fun stop(byUser: Boolean) {
        updatePlaybackState {
            setState(if (byUser) PlaybackStateCompat.STATE_NONE else PlaybackStateCompat.STATE_STOPPED, 0, 1F)
        }
        isInitialized = false
        audioPlayer.stop()
        isPlayingCallback(false, byUser)
    }

    override suspend fun skipTo(position: Int) {
        if (queueManager.currentAudioIndex == position) {
            Timber.d("Not skipping to index=$position")
            return
        }
        val audio = queueManager.queue.getOrNull(position) ?: return
        queueManager.setCurrentAudioId(audio.id)
        queueManager.refreshCurrentAudio()
        playAudio(audio)
        updatePlaybackState()
    }

    override fun release() {
        queueManager.clear()
        mediaSession.apply {
            isActive = false
            release()
        }
        audioPlayer.release()
    }

    override fun onPlayingState(playing: OnIsPlaying<SSAudioPlayer>) {
        this.isPlayingCallback = playing
    }

    override fun onPrepared(prepared: OnPrepared<SSAudioPlayer>) {
        this.preparedCallback = prepared
    }

    override fun onError(error: OnError<SSAudioPlayer>) {
        this.errorCallback = error
    }

    override fun onCompletion(completion: OnCompletion<SSAudioPlayer>) {
        this.completionCallback = completion
    }

    override fun onMetaDataChanged(metaDataChanged: OnMetaDataChanged) {
        this.metaDataChangedCallback = metaDataChanged
    }

    override fun updatePlaybackState(applier: PlaybackStateCompat.Builder.() -> Unit) {
        applier(stateBuilder)
        stateBuilder.setExtras(
            stateBuilder.build().extras + bundleOf(
                QUEUE_CURRENT_INDEX to queueManager.currentAudioIndex,
                QUEUE_HAS_PREVIOUS to (queueManager.previousAudioIndex != null),
                QUEUE_HAS_NEXT to (queueManager.nextAudioIndex != null)
            )
        )
        setPlaybackState(stateBuilder.build())
    }

    override fun setPlaybackState(state: PlaybackStateCompat) {
        mediaSession.setPlaybackState(state)
        state.extras?.let { bundle ->
            mediaSession.setRepeatMode(bundle.getInt(REPEAT_MODE))
            mediaSession.setShuffleMode(bundle.getInt(SHUFFLE_MODE))
        }
    }

    override suspend fun setDataFromMediaId(_mediaId: String, extras: Bundle) {
        val mediaId = _mediaId.toMediaId()
        val audioId = extras.getString(QUEUE_MEDIA_ID_KEY) ?: mediaId.value
        val audio = repository.findAudioFile(audioId) ?: run {
            Timber.e("Couldn't find mediaId: $audioId")
            return
        }
        playAudio(audio)
    }

    override fun resetMedia() {
        launch {
            val audio = repository.findAudioFile(queueManager.currentAudioId) ?: return@launch
            setMetaData(audio)
        }
    }

    private fun goToStart() {
        isInitialized = false

        stop()
    }

    private fun setMetaData(audio: AudioFile) {
        val player = this
        launch {
            val mediaMetadata = audio.toMediaMetadata(metadataBuilder)

            mediaSession.setMetadata(mediaMetadata.build())
            metaDataChangedCallback(player)

            val bitmap = context.getBitmap(audio.image.toUri(), COVER_IMAGE_SIZE)
            val updatedMetadata = mediaMetadata.apply { putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap) }.build()

            mediaSession.setMetadata(updatedMetadata)
            metaDataChangedCallback(player)
        }
    }
}

operator fun Bundle?.plus(other: Bundle?) = this.apply { (this ?: Bundle()).putAll(other ?: Bundle()) }
