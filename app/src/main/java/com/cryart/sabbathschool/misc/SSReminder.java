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


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.commonsware.cwac.wakeful.WakefulIntentService;

import java.util.Calendar;

public class SSReminder implements WakefulIntentService.AlarmListener {
    public void scheduleAlarms(AlarmManager _SSAlarmManager, PendingIntent _SSAlarmIntent, Context context){
        WakefulIntentService.cancelAlarms(context);

        SharedPreferences ssPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        if (ssPreferences.getBoolean(
                SSConstants.SS_SETTINGS_REMINDER_ENABLED_KEY,
                SSConstants.SS_SETTINGS_REMINDER_ENABLED_DEFAULT_VALUE)) {

            long currentTimeInMillis = System.currentTimeMillis();
            Calendar _SSReminderTime = Calendar.getInstance();
            _SSReminderTime.setTimeInMillis(currentTimeInMillis);

            String ss_settings_reminder_time = ssPreferences.getString(SSConstants.SS_SETTINGS_REMINDER_TIME_KEY, SSConstants.SS_SETTINGS_REMINDER_TIME_DEFAULT_VALUE);

            _SSReminderTime.set(Calendar.HOUR_OF_DAY, SSHelper.parseHourFromString(ss_settings_reminder_time, SSConstants.SS_REMINDER_TIME_SETTINGS_FORMAT));
            _SSReminderTime.set(Calendar.MINUTE, SSHelper.parseMinuteFromString(ss_settings_reminder_time, SSConstants.SS_REMINDER_TIME_SETTINGS_FORMAT));
            _SSReminderTime.set(Calendar.SECOND, 0);

            if (_SSReminderTime.getTimeInMillis() < currentTimeInMillis){
                _SSReminderTime.add(Calendar.DATE, 1);
            }

            _SSAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, _SSReminderTime.getTimeInMillis(), AlarmManager.INTERVAL_DAY, _SSAlarmIntent);
        }
    }

    public void sendWakefulWork(Context ctxt) {
        WakefulIntentService.sendWakefulWork(ctxt, SSReminderService.class);
    }

    public long getMaxAge() {
        return AlarmManager.INTERVAL_FIFTEEN_MINUTES * 2;
    }
}
