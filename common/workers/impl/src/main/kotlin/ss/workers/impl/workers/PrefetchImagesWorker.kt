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

package ss.workers.impl.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.ss.storage.db.dao.QuarterliesDao
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@HiltWorker
internal class PrefetchImagesWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val quarterliesDao: QuarterliesDao,
) : CoroutineWorker(appContext, workerParams) {

    private val dispatcher = Dispatchers.IO.limitedParallelism(5)

    override suspend fun doWork(): Result {
        val language = workerParams.inputData.getString(LANGUAGE_KEY) ?: return Result.failure()

        withContext(dispatcher) {
            val images = quarterliesDao.getCovers(language)

            Timber.i("Caching [${images.size}] images...")

            images.forEach { image ->
                val request = ImageRequest.Builder(appContext)
                    .data(image)
                    .memoryCachePolicy(CachePolicy.DISABLED)
                    .dispatcher(dispatcher)
                    .build()

                appContext.imageLoader.enqueue(request)
            }
        }

        return Result.success()
    }

    companion object {
        const val LANGUAGE_KEY = "input:language"

        val uniqueWorkName: String = PrefetchImagesWorker::class.java.simpleName
    }
}
