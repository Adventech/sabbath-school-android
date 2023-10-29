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
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package ss.workers.impl

import android.annotation.SuppressLint
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import ss.workers.impl.workers.PrefetchImagesWorker
import ss.workers.impl.workers.SyncQuarterliesWorker
import ss.workers.impl.workers.SyncQuarterlyWorker

@SuppressLint("RestrictedApi")
class WorkSchedulerImplTest {

    private val fakeWorkManager = FakeWorkManager()

    private val underTest = WorkSchedulerImpl(fakeWorkManager)

    @Test
    fun `preFetchImages - verify constrains`() {
        underTest.preFetchImages("en")

        with(fakeWorkManager) {
            capturedUniqueWorkName shouldBeEqualTo PrefetchImagesWorker.uniqueWorkName
            capturedExistingWorkPolicy shouldBeEqualTo ExistingWorkPolicy.KEEP
            with(capturedOneTimeWorkRequest!!.workSpec) {
                input.getString(PrefetchImagesWorker.LANGUAGE_KEY) shouldBeEqualTo "en"
                with(constraints) {
                    requiredNetworkType shouldBeEqualTo NetworkType.UNMETERED
                    requiresBatteryNotLow() shouldBe true
                    requiresStorageNotLow() shouldBe true
                }
            }
        }
    }

    @Test
    fun `syncQuarterly - verify constrains`() {
        underTest.syncQuarterly("index")

        with(fakeWorkManager) {
            capturedUniqueWorkName shouldBeEqualTo SyncQuarterlyWorker.uniqueWorkName
            capturedExistingWorkPolicy shouldBeEqualTo ExistingWorkPolicy.REPLACE
            with(capturedOneTimeWorkRequest!!.workSpec) {
                input.getString(SyncQuarterlyWorker.QUARTERLY_INDEX) shouldBeEqualTo "index"
                with(constraints) {
                    requiredNetworkType shouldBeEqualTo NetworkType.CONNECTED
                    requiresStorageNotLow() shouldBe true
                }
                expedited shouldBe true
            }
        }
    }

    @Test
    fun `syncQuarterlies - verify constrains`() {
        underTest.syncQuarterlies()

        with(fakeWorkManager) {
            capturedUniqueWorkName shouldBeEqualTo SyncQuarterliesWorker.uniqueWorkName
            capturedExistingPeriodicWorkPolicy shouldBeEqualTo ExistingPeriodicWorkPolicy.UPDATE
            with(capturedPeriodicWorkRequest!!.workSpec) {
                with(constraints) {
                    requiredNetworkType shouldBeEqualTo NetworkType.NOT_REQUIRED
                }
            }
        }
    }
}
