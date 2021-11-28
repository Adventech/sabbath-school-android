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

package com.cryart.sabbathschool.reminder

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.cryart.sabbathschool.R
import com.cryart.sabbathschool.core.extensions.prefs.SSPrefs
import com.cryart.sabbathschool.settings.DailyReminder
import com.cryart.sabbathschool.ui.splash.SplashActivity
import org.joda.time.DateTime
import timber.log.Timber

class DailyReminderManager constructor(
    private val context: Context,
    private val alarmManager: AlarmManager,
    private val notificationManager: NotificationManagerCompat,
    private val ssPrefs: SSPrefs,
    private val dateNow: DateTime? = null
) : DailyReminder {

    private fun getPendingIntent(
        create: Boolean
    ): PendingIntent? = Intent(context, ReminderReceiver::class.java).let { intent ->
        val flag = if (create) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                PendingIntent.FLAG_IMMUTABLE
            else
                PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            else
                PendingIntent.FLAG_NO_CREATE
        }

        PendingIntent.getBroadcast(context, 0, intent, flag)
    }

    private fun getTriggerAtMillis(): Long {
        val dateTimeNow = dateNow ?: DateTime.now()
        val time = ssPrefs.getReminderTime()
        val reminderTime = dateTimeNow
            .withTimeAtStartOfDay()
            .plusHours(time.hour)
            .plusMinutes(time.min)

        return if (dateTimeNow.isBefore(reminderTime)) {
            reminderTime.millis
        } else {
            reminderTime.plusDays(1).millis
        }
    }

    fun scheduleReminder() {
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            getTriggerAtMillis(),
            getPendingIntent(true)
        )

        ssPrefs.setReminderScheduled()
    }

    override fun reSchedule() {
        getPendingIntent(false)?.let {
            Timber.i("Cancelling Alarm...")
            alarmManager.cancel(it)
        }

        if (ssPrefs.reminderEnabled()) {
            scheduleReminder()
        }
    }

    override fun showNotification(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName: String = context.getString(R.string.ss_app_name)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, channelName, importance)
            notificationManager.createNotificationChannel(channel)
        }

        val contentIntent = Intent(context, SplashActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            PendingIntent.FLAG_IMMUTABLE
        else
            PendingIntent.FLAG_CANCEL_CURRENT

        val pendingIntent = PendingIntent.getActivity(
            context, 0,
            contentIntent, flag
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_notification)
            .setContentTitle(context.getString(R.string.ss_app_name))
            .setColor(ContextCompat.getColor(context, R.color.ss_theme_primary))
            .addAction(0, context.getString(R.string.ss_menu_read_now), pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(1000, 1000))
            .setContentIntent(pendingIntent)
            .setContentText(context.getString(R.string.ss_settings_reminder_text))

        notificationManager.notify(1, builder.build())
    }

    companion object {
        private const val CHANNEL_ID = "ss_notification_channel"
    }
}
