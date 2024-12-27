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

package ss.resources.impl.work

import android.content.Context
import android.net.Uri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.adventech.blockkit.model.resource.ResourceFont
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.buffer
import okio.sink
import ss.foundation.coroutines.DispatcherProvider
import ss.lessons.api.ResourcesApi
import ss.libraries.storage.api.dao.FontFilesDao
import ss.libraries.storage.api.entity.FontFileEntity
import timber.log.Timber
import java.io.File

@HiltWorker
class DownloadResourceWork @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val fontFilesDao: FontFilesDao,
    private val resourcesApi: ResourcesApi,
    private val dispatcherProvider: DispatcherProvider,
    private val okHttpClient: OkHttpClient,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val index = inputData.getString(INDEX_KEY) ?: return Result.failure()

        val resource = withContext(dispatcherProvider.default) {
            resourcesApi.resource(index)
                .body() ?: return@withContext null
        } ?: return Result.retry()

        withContext(dispatcherProvider.io) {
            downloadFiles(resource.fonts.orEmpty())
        }

        return Result.success()
    }

    private suspend fun downloadFiles(fonts: List<ResourceFont>) {
        fonts.forEach { font ->
            if (fontFilesDao.find(font.name) == null) {
                val (fileName, file) = downloadFile(appContext, font)
                if (file != null) {
                    fileName?.let {
                        fontFilesDao.insertItem(
                            FontFileEntity(
                                fileName = fileName,
                                name = font.name,
                            )
                        )
                    }
                }
            }
        }
    }

    private fun downloadFile(context: Context, resourceFont: ResourceFont): Pair<String?, File?> {
        val uri = Uri.parse(resourceFont.src)
        val fileName = uri.lastPathSegment
        val fontsDir = File(context.filesDir, FONTS_PATH)
        if (!fontsDir.exists()) {
            fontsDir.mkdirs()
        }
        val destination = File(context.filesDir, "$FONTS_PATH/$fileName")
        val request = Request.Builder().url(uri.toString()).build()
        return try {
            val response = okHttpClient.newCall(request).execute()
            val body =
                response.body
                    ?: run {
                        Timber.e("Failed to download file: $fileName")
                        return fileName to null
                    }
            destination.sink().buffer().use { sink -> sink.writeAll(body.source()) }
            fileName to destination
        } catch (error: Throwable) {
            Timber.e(error)
            fileName to null
        }
    }

    companion object {
        const val INDEX_KEY = "resource-index"
        private const val FONTS_PATH = "fonts"
    }
}
