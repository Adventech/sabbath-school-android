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

package ss.workers.impl.workers

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PrefetchImagesWorkerTest {

    private val appContext: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `doWork - returns failure when missing input`() = runTest {
        val worker = TestListenableWorkerBuilder<PrefetchImagesWorker>(
            context = appContext,
        ).setWorkerFactory(TestWorkerFactory())
            .build()

        val result = worker.doWork()

        result shouldBeEqualTo ListenableWorker.Result.failure()
    }

    @Test
    fun `doWork - returns success when done caching quarterly language`() = runTest {
        val worker = TestListenableWorkerBuilder<PrefetchImagesWorker>(
            context = appContext,
            inputData = workDataOf(PrefetchImagesWorker.LANGUAGE_KEY to "en")
        ).setWorkerFactory(TestWorkerFactory())
            .build()

        val result = worker.doWork()

        result shouldBeEqualTo ListenableWorker.Result.success()
    }

    @Test
    fun `doWork - returns success when done caching images`() = runTest {
        val worker = TestListenableWorkerBuilder<PrefetchImagesWorker>(
            context = appContext,
            inputData = workDataOf(PrefetchImagesWorker.IMAGES_KEY to arrayOf("image1", "image2"))
        ).setWorkerFactory(TestWorkerFactory())
            .build()

        val result = worker.doWork()

        result shouldBeEqualTo ListenableWorker.Result.success()
    }

}
