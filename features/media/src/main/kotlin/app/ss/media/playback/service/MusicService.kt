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

package app.ss.media.playback.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.media3.common.AudioAttributes
import androidx.media3.datasource.DataSourceBitmapLoader
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CacheBitmapLoader
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import app.ss.media.playback.DEFAULT_FORWARD
import app.ss.media.playback.DEFAULT_REWIND
import app.ss.media.playback.MediaNotifications
import app.ss.media.playback.SAFE_FLAG_IMMUTABLE
import app.ss.media.playback.receivers.BecomingNoisyReceiver
import app.ss.translations.R
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

private const val LOG_TAG = "SS_MusicService"

@AndroidEntryPoint
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
class MusicService : MediaLibraryService() {

    @Inject
    lateinit var mediaNotifications: MediaNotifications

    private var becomingNoisyReceiver: BecomingNoisyReceiver? = null

    private lateinit var mediaLibrarySession: MediaLibrarySession
    private lateinit var player: ExoPlayer
    private val librarySessionCallback = CustomMediaLibrarySessionCallback()

    override fun onCreate() {
        super.onCreate()

        player =
            ExoPlayer.Builder(this)
                .setAudioAttributes(AudioAttributes.DEFAULT, /* handleAudioFocus= */ true)
                .setSeekBackIncrementMs(DEFAULT_REWIND)
                .setSeekForwardIncrementMs(DEFAULT_FORWARD)
                .build()
        mediaLibrarySession =
            MediaLibrarySession.Builder(this, player, librarySessionCallback)
                .setId(getString(R.string.ss_app_name))
                .setSessionActivity(getSingleTopActivity())
                .setBitmapLoader(CacheBitmapLoader(DataSourceBitmapLoader(/* context= */ this)))
                .build()

        setListener(MediaSessionServiceListener())
    }

    private fun getSingleTopActivity(): PendingIntent {
        return PendingIntent.getActivity(
            applicationContext, 0,
            packageManager.getLaunchIntentForPackage(packageName), SAFE_FLAG_IMMUTABLE
        )
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession {
        return mediaLibrarySession
    }

    private inner class MediaSessionServiceListener : Listener {
        override fun onForegroundServiceStartNotAllowedException() {
            Timber.tag(LOG_TAG).i("MusicService: onForegroundServiceStartNotAllowedException")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                super.onForegroundServiceStartNotAllowedException()
            }
        }
    }

    private inner class CustomMediaLibrarySessionCallback : MediaLibrarySession.Callback {
        override fun onConnect(session: MediaSession, controller: MediaSession.ControllerInfo): MediaSession.ConnectionResult {
            Timber.tag(LOG_TAG).i("onConnect: $session, $controller")
            val availableSessionCommands =
                MediaSession.ConnectionResult.DEFAULT_SESSION_AND_LIBRARY_COMMANDS.buildUpon()
            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(availableSessionCommands.build())
                .build()
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            Timber.tag(LOG_TAG).i("onCustomCommand: $session, $args")
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }

        override fun onSubscribe(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<Void>> {
//            val children =
//                MediaItemTree.getChildren(parentId)
//                    ?: return Futures.immediateFuture(
//                        LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE)
//                    )
            //  session.notifyChildrenChanged(browser, parentId, children.size, params)
            return Futures.immediateFuture(LibraryResult.ofVoid())
        }
    }

    private fun startForeground() {
        if (IS_FOREGROUND) {
            Timber.tag(LOG_TAG).i("Tried to start foreground, but was already in foreground")
            return
        }
        Timber.tag(LOG_TAG).i("Starting foreground service")

//        val notification = mediaNotifications.buildNotification(musicPlayer.getSession())
//        if (isAtLeastApi(Build.VERSION_CODES.Q)) {
//            startForeground(NOTIFICATION_ID, notification, FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
//        } else {
//            startForeground(NOTIFICATION_ID, notification)
//        }
//        becomingNoisyReceiver?.register()
//        IS_FOREGROUND = true
    }

    private fun pauseForeground(removeNotification: Boolean) {
        if (!IS_FOREGROUND) {
            Timber.w("Tried to stop foreground, but was already NOT in foreground")
            return
        }
        Timber.d("Stopping foreground service")
        becomingNoisyReceiver?.unregister()
        stopForeground(if (removeNotification) STOP_FOREGROUND_REMOVE else STOP_FOREGROUND_DETACH)
        IS_FOREGROUND = false
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        player.run {
            if (!playWhenReady || mediaItemCount == 0) {
                stopSelf()
            }
        }
    }

    override fun onDestroy() {
        mediaLibrarySession.release()
        player.release()
        clearListener()
        super.onDestroy()
    }

    companion object {
        var IS_FOREGROUND = false
    }
}
