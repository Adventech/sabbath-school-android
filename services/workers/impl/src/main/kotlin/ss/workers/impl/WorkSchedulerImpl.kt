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

import androidx.annotation.VisibleForTesting
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import ss.workers.api.WorkScheduler
import ss.workers.impl.workers.PrefetchImagesWorker
import ss.workers.impl.workers.SyncQuarterliesWorker
import ss.workers.impl.workers.SyncQuarterlyWorker
import java.util.concurrent.TimeUnit

@VisibleForTesting
const val SYNC_REPEAT_INTERVAL = 12L

@VisibleForTesting
const val SYNC_FLEX_INTERVAL = 4L

internal class WorkSchedulerImpl constructor(
    private val workManager: WorkManager
) : WorkScheduler {

    override fun preFetchImages(language: String) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresBatteryNotLow(true)
            .setRequiresStorageNotLow(true)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<PrefetchImagesWorker>()
            .setConstraints(constraints)
            .setInputData(workDataOf(PrefetchImagesWorker.LANGUAGE_KEY to language))
            .build()

        workManager.enqueueUniqueWork(
            PrefetchImagesWorker.uniqueWorkName,
            ExistingWorkPolicy.KEEP,
            workRequest
        )
    }

    override fun syncQuarterly(index: String) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresStorageNotLow(true)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<SyncQuarterlyWorker>()
            .setConstraints(constraints)
            .setInputData(workDataOf(SyncQuarterlyWorker.QUARTERLY_INDEX to index))
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        workManager.enqueueUniqueWork(
            SyncQuarterlyWorker.uniqueWorkName,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    override fun syncQuarterlies() {
        val workRequest = PeriodicWorkRequestBuilder<SyncQuarterliesWorker>(
            SYNC_REPEAT_INTERVAL, TimeUnit.HOURS,
            SYNC_FLEX_INTERVAL, TimeUnit.HOURS,
        )
            .setBackoffCriteria(BackoffPolicy.LINEAR, SYNC_FLEX_INTERVAL, TimeUnit.HOURS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            SyncQuarterliesWorker.uniqueWorkName,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }
}
