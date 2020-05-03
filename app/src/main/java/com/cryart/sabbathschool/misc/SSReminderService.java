/*
 * Copyright (c) 2020 Adventech <info@adventech.io>
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

package com.cryart.sabbathschool.misc;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.cryart.sabbathschool.R;
import com.cryart.sabbathschool.ui.splash.SplashActivity;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import timber.log.Timber;

public class SSReminderService extends JobService {
    @Override
    public boolean onStartJob(JobParameters job) {
        Context context = getBaseContext();
        SSReminder.scheduleAlarms(context);
        try {
            String channelId = "ss_notification_channel";
            String channelName = getString(R.string.app_name);

            NotificationManager _SSNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel mChannel = new NotificationChannel(channelId, channelName, importance);
                _SSNotificationManager.createNotificationChannel(mChannel);
            }
            Intent _SSContentIntent = new Intent(context, SplashActivity.class);

            Intent _SSShareIntent = new Intent();

            _SSContentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            _SSShareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            _SSShareIntent.setAction(Intent.ACTION_SEND);
            _SSShareIntent.putExtra(Intent.EXTRA_TEXT, "");

            _SSShareIntent.setType("*/*");

            PendingIntent _SSPendingContentIntent = PendingIntent.getActivity(context, 0, _SSContentIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            PendingIntent _SSPendingShareIntent = PendingIntent.getActivity(context, 0, Intent.createChooser(_SSShareIntent, context.getString(R.string.ss_share)), PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder _SSNotificationBuilder =
                    new NotificationCompat.Builder(context, "ss_notification_channel")
                            .setSmallIcon(R.mipmap.ic_stat_notification)
                            .setContentTitle(context.getString(R.string.ss_app_name))
                            .setColor(Color.parseColor(SSColorTheme.getInstance().getColorPrimary()))
                            .addAction(0, context.getString(R.string.ss_menu_read_now), _SSPendingContentIntent)
                            .addAction(0, context.getString(R.string.ss_share), _SSPendingShareIntent)
                            .setAutoCancel(true)
                            .setVibrate(new long[]{1000, 1000})
                            .setContentIntent(_SSPendingContentIntent)
                            .setContentText(context.getString(R.string.ss_settings_reminder_text));

            _SSNotificationManager.notify(1, _SSNotificationBuilder.build());
        } catch (Exception e) {
            Timber.e(e);
        }

        return false; // Answers the question: "Is there still work going on?"
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false; // Answers the question: "Should this job be retried?"
    }
}
