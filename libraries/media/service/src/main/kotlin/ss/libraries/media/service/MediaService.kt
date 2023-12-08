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
import androidx.media3.common.AudioAttributes
import androidx.media3.datasource.DataSourceBitmapLoader
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CacheBitmapLoader
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import ss.libraries.media.api.DEFAULT_FORWARD
import ss.libraries.media.api.DEFAULT_REWIND
import timber.log.Timber

private const val LOG_TAG = "SS_MediaService"

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
abstract class MediaService : MediaSessionService() {

    abstract fun sessionId(): String

    open fun launchIntent(): Intent? = packageManager.getLaunchIntentForPackage(packageName)

    private val sessionCallback: CustomMediaSessionCallback by lazy {
        CustomMediaSessionCallback(applicationContext, sessionId())
    }
    private lateinit var mediaSession: MediaSession

    override fun onCreate() {
        super.onCreate()
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
        mediaSession =
            MediaSession.Builder(this, player)
                .setId(sessionId())
                .setSessionActivity(getSingleTopActivity())
                .setBitmapLoader(CacheBitmapLoader(DataSourceBitmapLoader(this)))
                .setCallback(sessionCallback)
                .setCustomLayout(sessionCallback.customCommands)
                .build()
    }

    private fun getSingleTopActivity(): PendingIntent {
        return PendingIntent.getActivity(
            applicationContext,
            0,
            launchIntent(),
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession {
        return mediaSession
    }

    private inner class MediaSessionServiceListener : Listener {
        override fun onForegroundServiceStartNotAllowedException() {
            Timber.tag(LOG_TAG).i("MediaService: onForegroundServiceStartNotAllowedException")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                super.onForegroundServiceStartNotAllowedException()
            }
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession.player
        player.run {
            if (!playWhenReady || mediaItemCount == 0) {
                stopSelf()
            }
        }
    }

    override fun onDestroy() {
        mediaSession.run {
            player.release()
            release()
        }
        clearListener()
        super.onDestroy()
    }
}
