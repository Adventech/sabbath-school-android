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
import androidx.core.app.NotificationManagerCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cryart.sabbathschool.core.extensions.prefs.SSPrefs
import com.cryart.sabbathschool.core.model.ReminderTime
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DailyReminderManagerTest {

    private val mockAlarmManager: AlarmManager = mockk(relaxed = true)
    private val mockNotificationManager: NotificationManagerCompat = mockk(relaxed = true)
    private val mockSSPrefs: SSPrefs = mockk(relaxed = true)

    // 07:00
    private val dateTimeNow = DateTime.now()
        .withTimeAtStartOfDay()
        .plusHours(7)

    private lateinit var testSubject: DailyReminderManager

    @Before
    fun setup() {
        every { mockSSPrefs.getReminderTime() }.returns(ReminderTime(6, 30))

        testSubject = DailyReminderManager(
            ApplicationProvider.getApplicationContext(),
            mockAlarmManager,
            mockNotificationManager,
            mockSSPrefs,
            dateNow = dateTimeNow
        )
    }

    @Test
    fun `should schedule repeating alarm - next day`() {
        val alarmTime = dateTimeNow
            .withTimeAtStartOfDay()
            .plusHours(6)
            .plusMinutes(30)
            .plusDays(1)

        testSubject.scheduleReminder()

        verify {
            mockAlarmManager.set(
                eq(AlarmManager.RTC_WAKEUP),
                eq(alarmTime.millis),
                any()
            )
        }

        verify { mockSSPrefs.setReminderScheduled() }
    }

    @Test
    fun `should schedule repeating alarm - same day`() {
        // 5 am
        val dateNow = DateTime.now()
            .withTimeAtStartOfDay()
            .plusHours(5)

        val alarmTime = DateTime.now()
            .withTimeAtStartOfDay()
            .plusHours(6)
            .plusMinutes(30)

        testSubject = DailyReminderManager(
            ApplicationProvider.getApplicationContext(),
            mockAlarmManager,
            mockNotificationManager,
            mockSSPrefs,
            dateNow
        )

        testSubject.scheduleReminder()

        verify {
            mockAlarmManager.set(
                eq(AlarmManager.RTC_WAKEUP),
                eq(alarmTime.millis),
                any()
            )
        }

        verify { mockSSPrefs.setReminderScheduled() }
    }

    @Test
    fun `should not schedule if pref not enabled`() {
        every { mockSSPrefs.reminderEnabled() }.returns(false)

        val alarmTime = dateTimeNow
            .withTimeAtStartOfDay()
            .plusHours(6)
            .plusMinutes(30)

        testSubject.reSchedule()

        verify(inverse = true) {
            mockAlarmManager.set(
                eq(AlarmManager.RTC_WAKEUP),
                eq(alarmTime.millis),
                any()
            )
        }
    }

    @Test
    fun `should show reminder notification`() {
        testSubject.showNotification(ApplicationProvider.getApplicationContext())

        verify {
            mockNotificationManager.notify(
                eq(1),
                any()
            )
        }
    }
}
