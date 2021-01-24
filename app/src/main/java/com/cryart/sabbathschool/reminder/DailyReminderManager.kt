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

import com.cryart.sabbathschool.core.extensions.prefs.SSPrefs
import com.cryart.sabbathschool.settings.DailyReminder
import com.evernote.android.job.JobManager
import com.evernote.android.job.JobRequest
import java.util.concurrent.TimeUnit
import org.joda.time.DateTime
import org.joda.time.Duration
import timber.log.Timber

class DailyReminderManager constructor(
    private val jobManager: JobManager,
    private val ssPrefs: SSPrefs,
    private val jobRequestBuilder: JobRequest.Builder = JobRequest.Builder(SSReminderJob.TAG),
    private val dateNow: DateTime? = null
) : DailyReminder {

    private val dateTimeNow: DateTime get() = dateNow ?: DateTime.now()

    private val delayMinutes: Long
        get() {
            val time = ssPrefs.getReminderTime()
            val reminderTime = dateTimeNow
                .withTimeAtStartOfDay()
                .plusHours(time.hour)
                .plusMinutes(time.min)

            return if (dateTimeNow.isBefore(reminderTime)) {
                Duration(dateTimeNow, reminderTime).standardMinutes
            } else {
                Duration(dateTimeNow, reminderTime.plusDays(1)).standardMinutes
            }
        }

    fun scheduleReminder() {
        if (ssPrefs.getReminderJobId() != null) {
            Timber.i("Reminder scheduled")
            return
        }

        val exact = if (delayMinutes <= 0) {
            TimeUnit.HOURS.toMillis(24)
        } else {
            TimeUnit.MINUTES.toMillis(delayMinutes)
        }
        val jobId = jobRequestBuilder
            .setExact(exact)
            .build()
            .schedule()

        ssPrefs.setReminderJobId(jobId)
    }

    fun cancelReminder() {
        jobManager.cancelAll()
        ssPrefs.setReminderJobId(null)
    }

    override fun reSchedule() {
        cancelReminder()
        scheduleReminder()
    }
}
