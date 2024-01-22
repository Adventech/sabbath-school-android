/*
 * Copyright (c) 2024. Adventech <info@adventech.io>
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
import android.app.PendingIntent
import androidx.lifecycle.LiveData
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.Operation
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkContinuation
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import androidx.work.WorkRequest
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/** Fake implementation of [WorkManager] for use in tests. */
@SuppressLint("RestrictedApi")
class FakeWorkManager : WorkManager() {

    var capturedUniqueWorkName: String? = null
        private set
    var capturedExistingWorkPolicy: ExistingWorkPolicy? = null
        private set
    var capturedExistingPeriodicWorkPolicy: ExistingPeriodicWorkPolicy? = null
        private set
    var capturedOneTimeWorkRequest: OneTimeWorkRequest? = null
        private set
    var capturedPeriodicWorkRequest: PeriodicWorkRequest? = null
        private set

    override fun getConfiguration(): Configuration {
        TODO("Not yet implemented")
    }

    override fun enqueue(requests: MutableList<out WorkRequest>): Operation {
        TODO("Not yet implemented")
    }

    override fun beginWith(work: MutableList<OneTimeWorkRequest>): WorkContinuation {
        TODO("Not yet implemented")
    }

    override fun beginUniqueWork(uniqueWorkName: String, existingWorkPolicy: ExistingWorkPolicy, work: MutableList<OneTimeWorkRequest>): WorkContinuation {
        TODO("Not yet implemented")
    }

    override fun enqueueUniqueWork(
        uniqueWorkName: String,
        existingWorkPolicy: ExistingWorkPolicy,
        work: MutableList<OneTimeWorkRequest>
    ): Operation {
        capturedUniqueWorkName = uniqueWorkName
        capturedExistingWorkPolicy = existingWorkPolicy
        capturedOneTimeWorkRequest = work.firstOrNull()
        return FakeOperation()
    }

    override fun enqueueUniquePeriodicWork(
        uniqueWorkName: String,
        existingPeriodicWorkPolicy: ExistingPeriodicWorkPolicy,
        periodicWork: PeriodicWorkRequest
    ): Operation {
        capturedUniqueWorkName = uniqueWorkName
        capturedExistingPeriodicWorkPolicy = existingPeriodicWorkPolicy
        capturedPeriodicWorkRequest = periodicWork
        return FakeOperation()
    }

    override fun cancelWorkById(id: UUID): Operation {
        TODO("Not yet implemented")
    }

    override fun cancelAllWorkByTag(tag: String): Operation {
        TODO("Not yet implemented")
    }

    override fun cancelUniqueWork(uniqueWorkName: String): Operation {
        TODO("Not yet implemented")
    }

    override fun cancelAllWork(): Operation {
        TODO("Not yet implemented")
    }

    override fun createCancelPendingIntent(id: UUID): PendingIntent {
        TODO("Not yet implemented")
    }

    override fun pruneWork(): Operation {
        TODO("Not yet implemented")
    }

    override fun getLastCancelAllTimeMillisLiveData(): LiveData<Long> {
        TODO("Not yet implemented")
    }

    override fun getLastCancelAllTimeMillis(): ListenableFuture<Long> {
        TODO("Not yet implemented")
    }

    override fun getWorkInfoByIdLiveData(id: UUID): LiveData<WorkInfo> {
        TODO("Not yet implemented")
    }

    override fun getWorkInfoByIdFlow(id: UUID): Flow<WorkInfo> {
        TODO("Not yet implemented")
    }

    override fun getWorkInfoById(id: UUID): ListenableFuture<WorkInfo> {
        TODO("Not yet implemented")
    }

    override fun getWorkInfosByTagLiveData(tag: String): LiveData<MutableList<WorkInfo>> {
        TODO("Not yet implemented")
    }

    override fun getWorkInfosByTagFlow(tag: String): Flow<MutableList<WorkInfo>> {
        TODO("Not yet implemented")
    }

    override fun getWorkInfosByTag(tag: String): ListenableFuture<MutableList<WorkInfo>> {
        TODO("Not yet implemented")
    }

    override fun getWorkInfosForUniqueWorkLiveData(uniqueWorkName: String): LiveData<MutableList<WorkInfo>> {
        TODO("Not yet implemented")
    }

    override fun getWorkInfosForUniqueWorkFlow(uniqueWorkName: String): Flow<MutableList<WorkInfo>> {
        TODO("Not yet implemented")
    }

    override fun getWorkInfosForUniqueWork(uniqueWorkName: String): ListenableFuture<MutableList<WorkInfo>> {
        TODO("Not yet implemented")
    }

    override fun getWorkInfosLiveData(workQuery: WorkQuery): LiveData<MutableList<WorkInfo>> {
        TODO("Not yet implemented")
    }

    override fun getWorkInfosFlow(workQuery: WorkQuery): Flow<MutableList<WorkInfo>> {
        TODO("Not yet implemented")
    }

    override fun getWorkInfos(workQuery: WorkQuery): ListenableFuture<MutableList<WorkInfo>> {
        TODO("Not yet implemented")
    }

    override fun updateWork(request: WorkRequest): ListenableFuture<UpdateResult> {
        TODO("Not yet implemented")
    }

    private inner class FakeOperation : Operation {
        override fun getState(): LiveData<Operation.State> {
            TODO("Not yet implemented")
        }

        override fun getResult(): ListenableFuture<Operation.State.SUCCESS> {
            TODO("Not yet implemented")
        }
    }
}
