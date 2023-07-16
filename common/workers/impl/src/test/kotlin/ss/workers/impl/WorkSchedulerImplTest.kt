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
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package ss.workers.impl

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import ss.workers.api.WorkScheduler
import ss.workers.impl.workers.PrefetchImagesWorker

class WorkSchedulerImplTest {

    private val mockWorkManager: WorkManager = mockk()
    private val requestSlot = slot<OneTimeWorkRequest>()

    private lateinit var scheduler: WorkScheduler

    @Before
    fun setup() {
        every {
            mockWorkManager.enqueueUniqueWork(
                PrefetchImagesWorker.uniqueWorkName,
                ExistingWorkPolicy.KEEP,
                capture(requestSlot)
            )
        } answers { mockk() }

        scheduler = WorkSchedulerImpl(
            workManager = mockWorkManager
        )
    }

    @Test
    fun `verify constrains`() {
        scheduler.preFetchImages("en")

        val workRequest = requestSlot.captured

        verify {
            mockWorkManager.enqueueUniqueWork(
                PrefetchImagesWorker.uniqueWorkName,
                ExistingWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}
