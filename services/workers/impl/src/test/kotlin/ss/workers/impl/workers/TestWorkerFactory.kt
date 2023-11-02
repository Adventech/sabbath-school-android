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

package ss.workers.impl.workers

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import ss.foundation.coroutines.test.TestDispatcherProvider
import ss.lessons.api.ContentSyncProvider
import ss.lessons.test.FakeContentSyncProvider

class TestWorkerFactory(
    private val contentSyncProvider: ContentSyncProvider = FakeContentSyncProvider()
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? = when (workerClassName) {
        PrefetchImagesWorker::class.java.name -> PrefetchImagesWorker(
            appContext = appContext,
            workerParams = workerParameters,
            quarterliesDao = FakeQuarterliesDao()
        )

        SyncQuarterlyWorker::class.java.name -> SyncQuarterlyWorker(
            appContext = appContext,
            workerParams = workerParameters,
            contentSyncProvider = contentSyncProvider,
            dispatcherProvider = TestDispatcherProvider(),
        )

        SyncQuarterliesWorker::class.java.name -> SyncQuarterliesWorker(
            appContext = appContext,
            workerParams = workerParameters,
            contentSyncProvider = contentSyncProvider,
            dispatcherProvider = TestDispatcherProvider(),
        )

        else -> null
    }
}
