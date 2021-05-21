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
import com.cryart.sabbathschool.core.model.ReminderTime
import com.evernote.android.job.JobManager
import com.evernote.android.job.JobRequest
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.amshove.kluent.shouldBeEqualTo
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test

class DailyReminderManagerTest {

    private val mockJobManager: JobManager = mockk()
    private val mockRequestBuilder: JobRequest.Builder = mockk(relaxed = true)
    private val mockJobRequest: JobRequest = mockk()
    private val mockSSPrefs: SSPrefs = mockk()

    private lateinit var testSubject: DailyReminderManager

    @Before
    fun setup() {
        every { mockSSPrefs.getReminderTime() }.returns(ReminderTime(6, 30))
        every { mockSSPrefs.setReminderJobId(any()) }.returns(Unit)

        every { mockJobRequest.schedule() }.returns(1)
        every { mockRequestBuilder.setExact(any()) }.returns(mockRequestBuilder)
        every { mockRequestBuilder.build() }.returns(mockJobRequest)
    }

    @Test
    fun `should set an initial delay of 2 hours 30 minutes when scheduled at 4am`() {
        val dateTime4am = DateTime(2020, 5, 20, 4, 0)
        testSubject = DailyReminderManager(
            mockJobManager,
            mockSSPrefs,
            mockRequestBuilder,
            dateTime4am
        )

        val millisSlot: CapturingSlot<Long> = slot()

        every { mockSSPrefs.getReminderJobId() }.returns(null)

        testSubject.scheduleReminder()

        verify { mockSSPrefs.setReminderJobId(1) }
        verify {
            mockRequestBuilder.setExact(capture(millisSlot))
        }

        millisSlot.captured shouldBeEqualTo (2.5 * HOUR).toLong()
    }

    @Test
    fun `should set an initial delay of 23 hours when scheduled at 7, 30 am`() {
        val dateTime7am = DateTime(2020, 5, 20, 7, 30)
        testSubject = DailyReminderManager(
            mockJobManager,
            mockSSPrefs,
            mockRequestBuilder,
            dateTime7am
        )

        val millisSlot: CapturingSlot<Long> = slot()

        every { mockSSPrefs.getReminderJobId() }.returns(null)

        testSubject.scheduleReminder()

        verify { mockSSPrefs.setReminderJobId(1) }
        verify {
            mockRequestBuilder.setExact(capture(millisSlot))
        }

        millisSlot.captured shouldBeEqualTo (23 * HOUR)
    }

    @Test
    fun `should cancel job and remove id from prefs when cancelReminder is called`() {
        testSubject = DailyReminderManager(mockJobManager, mockSSPrefs, mockRequestBuilder)
        every { mockJobManager.cancelAll() }.returns(1)

        testSubject.cancelReminder()

        verify {
            mockJobManager.cancelAll()
            mockSSPrefs.setReminderJobId(null)
        }
    }

    @Test
    fun `should cancel and schedule when reschedule is called`() {
        val dateTime7am = DateTime(2020, 5, 20, 7, 30)
        testSubject = DailyReminderManager(
            mockJobManager,
            mockSSPrefs,
            mockRequestBuilder,
            dateTime7am
        )
        every { mockJobManager.cancelAll() }.returns(1)
        every { mockSSPrefs.getReminderJobId() }.returns(null)
        every { mockSSPrefs.reminderEnabled() }.returns(true)

        testSubject.reSchedule()

        verify {
            mockJobManager.cancelAll()
            mockSSPrefs.setReminderJobId(null)
            mockSSPrefs.setReminderJobId(1)
        }
    }

    @Test
    fun `should cancel and not schedule when reschedule is called and reminder is disabled`() {
        val dateTime7am = DateTime(2020, 5, 20, 7, 30)
        testSubject = DailyReminderManager(
            mockJobManager,
            mockSSPrefs,
            mockRequestBuilder,
            dateTime7am
        )
        every { mockJobManager.cancelAll() }.returns(1)
        every { mockSSPrefs.getReminderJobId() }.returns(null)
        every { mockSSPrefs.reminderEnabled() }.returns(false)

        testSubject.reSchedule()

        verify {
            mockJobManager.cancelAll()
            mockSSPrefs.setReminderJobId(null)
        }
        verify(inverse = true) {
            mockSSPrefs.setReminderJobId(1)
        }
    }

    companion object {
        private const val MINUTE = 60000L
        private const val HOUR = 60 * MINUTE
    }
}
