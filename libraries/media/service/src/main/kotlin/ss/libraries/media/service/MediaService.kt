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

package ss.libraries.media.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.media3.common.AudioAttributes
import androidx.media3.datasource.DataSourceBitmapLoader
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CacheBitmapLoader
import androidx.media3.session.CommandButton
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import ss.libraries.media.api.DEFAULT_FORWARD
import ss.libraries.media.api.DEFAULT_REWIND
import timber.log.Timber
import app.ss.translations.R as L10nR
import ss.libraries.media.service.R as MediaR

private const val LOG_TAG = "SS_MediaService"
private const val CUSTOM_COMMAND_REWIND = "app.ss.media.playback.REWIND"
private const val CUSTOM_COMMAND_FORWARD = "app.ss.media.playback.FORWARD"

@AndroidEntryPoint
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
class MediaService : MediaLibraryService() {

    private lateinit var customCommands: List<CommandButton>
    private val librarySessionCallback = CustomMediaLibrarySessionCallback()
    private lateinit var mediaLibrarySession: MediaLibrarySession

    override fun onCreate() {
        super.onCreate()
        customCommands =
            listOf(
                getRewindCommandButton(SessionCommand(CUSTOM_COMMAND_REWIND, Bundle.EMPTY)),
                getForwardCommandButton(SessionCommand(CUSTOM_COMMAND_FORWARD, Bundle.EMPTY)),
            )
        initializeSessionAndPlayer()

        setListener(MediaSessionServiceListener())
    }

    private fun initializeSessionAndPlayer() {
        val player =
            ExoPlayer.Builder(this)
                .setAudioAttributes(AudioAttributes.DEFAULT, /* handleAudioFocus= */ true)
                .setSeekBackIncrementMs(DEFAULT_REWIND)
                .setSeekForwardIncrementMs(DEFAULT_FORWARD)
                .build()
        mediaLibrarySession =
            MediaLibrarySession.Builder(this, player, librarySessionCallback)
                .setId(getString(L10nR.string.ss_app_name))
                .setSessionActivity(getSingleTopActivity())
                .setBitmapLoader(CacheBitmapLoader(DataSourceBitmapLoader(this)))
                .build()
    }

    private fun getSingleTopActivity(): PendingIntent {
        return PendingIntent.getActivity(
            applicationContext, 0,
            packageManager.getLaunchIntentForPackage(packageName), PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession {
        return mediaLibrarySession
    }

    private fun getRewindCommandButton(sessionCommand: SessionCommand): CommandButton {
        return CommandButton.Builder()
            .setDisplayName(getString(L10nR.string.ss_action_rewind))
            .setSessionCommand(sessionCommand)
            .setIconResId(MediaR.drawable.ic_audio_icon_backward)
            .build()
    }

    private fun getForwardCommandButton(sessionCommand: SessionCommand): CommandButton {
        return CommandButton.Builder()
            .setDisplayName(getString(L10nR.string.ss_action_forward))
            .setSessionCommand(sessionCommand)
            .setIconResId(MediaR.drawable.ic_audio_icon_forward)
            .build()
    }

    private inner class MediaSessionServiceListener : Listener {
        override fun onForegroundServiceStartNotAllowedException() {
            Timber.tag(LOG_TAG).i("MediaService: onForegroundServiceStartNotAllowedException")
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
            for (commandButton in customCommands) {
                // Add custom command to available session commands.
                commandButton.sessionCommand?.let { availableSessionCommands.add(it) }
            }
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
            when (customCommand.customAction) {
                CUSTOM_COMMAND_REWIND -> session.player.seekBack()
                CUSTOM_COMMAND_FORWARD -> session.player.seekForward()
            }
            session.setCustomLayout(customCommands)
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }

        override fun onSubscribe(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<Void>> {
            return Futures.immediateFuture(LibraryResult.ofVoid())
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaLibrarySession.player
        player.run {
            if (!playWhenReady || mediaItemCount == 0) {
                stopSelf()
            }
        }
    }

    override fun onDestroy() {
        mediaLibrarySession.run {
            player.release()
            release()
        }
        clearListener()
        super.onDestroy()
    }
}
