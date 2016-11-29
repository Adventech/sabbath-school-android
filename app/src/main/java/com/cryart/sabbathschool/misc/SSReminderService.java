/*
 * Copyright (c) 2016 Adventech <info@adventech.io>
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


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.crashlytics.android.Crashlytics;
import com.cryart.sabbathschool.R;
import com.cryart.sabbathschool.view.SSSplashActivity;

public class SSReminderService extends WakefulIntentService {
    public SSReminderService() {
        super("SSReminderService");
    }

    @Override
    protected void doWakefulWork(Intent intent) {
        Context context = getBaseContext();
        try {
            NotificationManager _SSNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            Intent _SSContentIntent = new Intent(context, SSSplashActivity.class);

            Intent _SSShareIntent = new Intent();

            _SSContentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            _SSShareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            _SSShareIntent.setAction(Intent.ACTION_SEND);
            _SSShareIntent.putExtra(Intent.EXTRA_TEXT, "");

            _SSShareIntent.setType("*/*");

            PendingIntent _SSPendingContentIntent = PendingIntent.getActivity(context, 0, _SSContentIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            PendingIntent _SSPendingShareIntent = PendingIntent.getActivity(context, 0, Intent.createChooser(_SSShareIntent, context.getString(R.string.ss_share)), PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder _SSNotificationBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.mipmap.ic_stat_notification)
                            .setContentTitle(context.getString(R.string.ss_app_name))
                            .setColor(Color.parseColor(SSColorTheme.getInstance().getColorPrimary()))
                            .addAction(0, context.getString(R.string.ss_menu_read_now), _SSPendingContentIntent)
                            .addAction(0, context.getString(R.string.ss_share), _SSPendingShareIntent)
                            .setAutoCancel(true)
                            .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                            .setVibrate(new long[] {1000, 1000})
                            .setContentIntent(_SSPendingContentIntent)
                            .setContentText(context.getString(R.string.ss_settings_reminder_text));

            _SSNotificationManager.notify(1, _SSNotificationBuilder.build());
        } catch (Exception e) {
            Crashlytics.log(e.getMessage());
        }
    }
}
