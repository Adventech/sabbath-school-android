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
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import org.junit.runner.RunWith
import ss.lessons.test.FakeContentSyncProvider

@RunWith(AndroidJUnit4::class)
class SyncQuarterliesWorkerTest {

    private val appContext: Context = ApplicationProvider.getApplicationContext()
    private val fakeContentSyncProvider = FakeContentSyncProvider()
    private val fakeWorkerFactory = TestWorkerFactory(fakeContentSyncProvider)

    @Test
    fun `doWork - returns success when sync is successful`() = runTest {
        fakeContentSyncProvider.defaultSyncResult = Result.success(Unit)

        val worker = TestListenableWorkerBuilder<SyncQuarterliesWorker>(
            context = appContext,
        ).setWorkerFactory(fakeWorkerFactory)
            .build()

        val result = worker.doWork()

        result shouldBeEqualTo ListenableWorker.Result.success()
    }

    @Test
    fun `doWork - returns retry when sync is not successful`() = runTest {
        fakeContentSyncProvider.defaultSyncResult = Result.failure(Throwable())

        val worker = TestListenableWorkerBuilder<SyncQuarterliesWorker>(
            context = appContext,
        ).setWorkerFactory(fakeWorkerFactory)
            .build()

        val result = worker.doWork()

        result shouldBeEqualTo ListenableWorker.Result.retry()
    }
}
