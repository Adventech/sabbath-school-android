/*
 * Copyright (c) 2021. Adventech <info@adventech.io>
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
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package app.ss.media.playback

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.DrawableRes
import androidx.media.app.NotificationCompat
import app.ss.media.playback.extensions.album
import app.ss.media.playback.extensions.artist
import app.ss.media.playback.extensions.artwork
import app.ss.media.playback.extensions.displayDescription
import app.ss.media.playback.extensions.isBuffering
import app.ss.media.playback.extensions.isOreo
import app.ss.media.playback.extensions.isPlaying
import app.ss.media.playback.extensions.title
import app.ss.media.playback.receivers.MediaButtonReceiver.Companion.buildMediaButtonPendingIntent
import app.ss.media.playback.service.MusicService
import com.cryart.sabbathschool.core.extensions.context.systemService
import app.ss.media.R

private const val CHANNEL_ID = "app.ss.media.NOW_PLAYING"
const val NOTIFICATION_ID = 0xb339

const val PREVIOUS = "action_previous"
const val NEXT = "action_next"
const val STOP_PLAYBACK = "action_stop"
const val PLAY_PAUSE = "action_play_or_pause"
const val REPEAT_ONE = "action_repeat_one"
const val REPEAT_ALL = "action_repeat_all"
const val PLAY_NEXT = "action_play_next"
const val UPDATE_QUEUE = "action_update_queue"
const val SET_MEDIA_STATE = "action_set_media_state"
const val PLAY_ACTION = "action_play"
const val PAUSE_ACTION = "action_pause"
const val BY_UI_KEY = "by_ui_key"

interface MediaNotifications {
    fun updateNotification(mediaSession: MediaSessionCompat)
    fun buildNotification(mediaSession: MediaSessionCompat): Notification
    fun clearNotifications()
}

internal class MediaNotificationsImpl constructor(
    private val context: Context
) : MediaNotifications {

    private val notificationManager: NotificationManager = context.systemService(Context.NOTIFICATION_SERVICE)

    override fun updateNotification(mediaSession: MediaSessionCompat) {
        if (!MusicService.IS_FOREGROUND) return
        notificationManager.notify(NOTIFICATION_ID, buildNotification(mediaSession))
    }

    override fun buildNotification(mediaSession: MediaSessionCompat): Notification {
        if (mediaSession.controller.metadata == null || mediaSession.controller.playbackState == null) {
            return createEmptyNotification()
        }

        val albumName = mediaSession.controller.metadata.album
        val artistName = mediaSession.controller.metadata.artist
        val trackName = mediaSession.controller.metadata.title
        val artwork = mediaSession.controller.metadata.artwork
        val isPlaying = mediaSession.isPlaying()
        val isBuffering = mediaSession.isBuffering()
        val description = mediaSession.controller.metadata.displayDescription

        val pm: PackageManager = context.packageManager
        val nowPlayingIntent = pm.getLaunchIntentForPackage(context.packageName)
        val clickIntent = PendingIntent.getActivity(context, 0, nowPlayingIntent, FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE)

        createNotificationChannel()

        val style = NotificationCompat.MediaStyle()
            .setMediaSession(mediaSession.sessionToken)
            .setShowCancelButton(true)
            .setShowActionsInCompactView(0, 1, 2)
            .setCancelButtonIntent(buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_STOP))

        val builder = androidx.core.app.NotificationCompat.Builder(context, CHANNEL_ID).apply {
            setStyle(style)
            setSmallIcon(R.drawable.ic_stat_notification)
            setLargeIcon(artwork)
            setContentIntent(clickIntent)
            setContentTitle(trackName)
            setContentText("$artistName - $albumName")
            setSubText(description)
            setColorized(true)
            setShowWhen(false)
            setVisibility(androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC)
            setDeleteIntent(buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_STOP))
            addAction(getPreviousAction(context))
            if (isBuffering)
                addAction(getBufferingAction())
            else
                addAction(getPlayPauseAction(context, if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_arrow))
            addAction(getNextAction(context))
            addAction(getStopAction(context))
        }

        if (artwork != null) {
            /*builder.color = Palette.from(artwork)
                .generate()
                .getDominantColor(Color.parseColor("#16053D"))*/
        }

        return builder.build()
    }

    override fun clearNotifications() {
        notificationManager.cancel(NOTIFICATION_ID)
    }

    private fun getBufferingAction(): androidx.core.app.NotificationCompat.Action {
        return androidx.core.app.NotificationCompat.Action(R.drawable.ic_hourglass_empty, "", null)
    }

    private fun getStopAction(context: Context): androidx.core.app.NotificationCompat.Action {
        val actionIntent = Intent(context, MusicService::class.java).apply { action = STOP_PLAYBACK }
        val pendingIntent = PendingIntent.getService(context, 0, actionIntent, FLAG_IMMUTABLE)
        return androidx.core.app.NotificationCompat.Action(R.drawable.ic_stop, "", pendingIntent)
    }

    private fun getPreviousAction(context: Context): androidx.core.app.NotificationCompat.Action {
        val actionIntent = Intent(context, MusicService::class.java).apply { action = PREVIOUS }
        val pendingIntent = PendingIntent.getService(context, 0, actionIntent, FLAG_IMMUTABLE)
        return androidx.core.app.NotificationCompat.Action(R.drawable.ic_skip_previous, "", pendingIntent)
    }

    private fun getPlayPauseAction(
        context: Context,
        @DrawableRes playButtonResId: Int
    ): androidx.core.app.NotificationCompat.Action {
        val actionIntent = Intent(context, MusicService::class.java).apply { action = PLAY_PAUSE }
        val pendingIntent = PendingIntent.getService(context, 0, actionIntent, FLAG_IMMUTABLE)
        return androidx.core.app.NotificationCompat.Action(playButtonResId, "", pendingIntent)
    }

    private fun getNextAction(context: Context): androidx.core.app.NotificationCompat.Action {
        val actionIntent = Intent(context, MusicService::class.java).apply {
            action = NEXT
        }
        val pendingIntent = PendingIntent.getService(context, 0, actionIntent, FLAG_IMMUTABLE)
        return androidx.core.app.NotificationCompat.Action(R.drawable.ic_skip_next, "", pendingIntent)
    }

    private fun createEmptyNotification(): Notification {
        createNotificationChannel()
        return androidx.core.app.NotificationCompat.Builder(context, CHANNEL_ID).apply {
            setSmallIcon(R.drawable.ic_stat_notification)
            setContentTitle(context.getString(R.string.ss_app_name))
            setColorized(true)
            setShowWhen(false)
            setVisibility(androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC)
        }.build()
    }

    private fun createNotificationChannel() {
        if (!isOreo()) return
        val name = context.getString(R.string.ss_media_notification_channel)
        val channel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_LOW).apply {
            description = context.getString(R.string.ss_media_notification_channel_description)
            setShowBadge(false)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        notificationManager.createNotificationChannel(channel)
    }
}
