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
package com.cryart.sabbathschool.settings

import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Context
import android.os.Build
import android.text.format.DateFormat
import android.util.AttributeSet
import android.widget.TimePicker
import androidx.preference.Preference
import ss.misc.SSConstants
import ss.misc.SSHelper
import java.util.Locale

class SSTimePickerPreference(
    context: Context,
    attrs: AttributeSet?
) : Preference(context, attrs), OnTimeSetListener {

    private val value: String? get() = sharedPreferences?.getString(key, SSConstants.SS_SETTINGS_REMINDER_TIME_DEFAULT_VALUE)

    override fun onClick() {
        super.onClick()
        val notificationTime = value
        TimePickerDialog(
            context,
            this,
            SSHelper.parseHourFromString(notificationTime, SSConstants.SS_REMINDER_TIME_SETTINGS_FORMAT),
            SSHelper.parseMinuteFromString(notificationTime, SSConstants.SS_REMINDER_TIME_SETTINGS_FORMAT),
            DateFormat.is24HourFormat(context)
        ).show()
    }

    @Suppress("DEPRECATION")
    override fun onTimeSet(timePicker: TimePicker, i: Int, i2: Int) {
        if (timePicker.isShown) {
            val hour: Int
            val minute: Int
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                hour = timePicker.hour
                minute = timePicker.minute
            } else {
                hour = timePicker.currentHour
                minute = timePicker.currentMinute
            }
            sharedPreferences?.edit()?.putString(
                SSConstants.SS_SETTINGS_REMINDER_TIME_KEY,
                String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
            )?.apply()
            summary = summary
        }
    }

    override fun getSummary(): String {
        return SSHelper.parseTimeAndReturnInFormat(
            value,
            SSConstants.SS_REMINDER_TIME_SETTINGS_FORMAT,
            DateFormat.getTimeFormat(context)
        )
    }
}
