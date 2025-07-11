/*
 * Copyright (c) 2025. Adventech <info@adventech.io>
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

package ss.foundation.android.intent

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import ss.foundation.coroutines.DispatcherProvider
import ss.foundation.coroutines.Scopable
import ss.foundation.coroutines.ioScopable
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import javax.inject.Inject

interface ShareIntentHelper {

    fun shareText(
        context: Context,
        text: String,
        chooserTitle: String? = null
    )

    fun shareFile(
        context: Context,
        fileUrl: String,
        fileName: String? = null,
        chooserTitle: String = "Share file via"
    )

    fun fileExists(
        fileUrl: String,
        fileName: String?,
    ): Flow<Boolean>
}

internal class ShareIntentHelperImpl @Inject constructor(
    @param:ApplicationContext private val appContext: Context,
    private val dispatcherProvider: DispatcherProvider
) : ShareIntentHelper, Scopable by ioScopable(dispatcherProvider) {

    private val exceptionLogger = CoroutineExceptionHandler { _, exception -> Timber.e(exception) }

    override fun shareText(
        context: Context,
        text: String,
        chooserTitle: String?
    ) {
        val shareIntent = ShareCompat.IntentBuilder(context)
            .setType("text/plain")
            .setText(text)
            .intent
        if (shareIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(Intent.createChooser(shareIntent, chooserTitle))
        }
    }

    override fun shareFile(
        context: Context,
        fileUrl: String,
        fileName: String?,
        chooserTitle: String
    ) {
        scope.launch(exceptionLogger) {
            // Download file from URL
            val finalFileName = fileName(fileUrl, fileName)
            val file = downloadFile(context, fileUrl, finalFileName) ?: return@launch

            // Get URI using FileProvider
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            // Determine MIME type from file extension
            val mimeType = when (file.extension.lowercase()) {
                "pdf" -> "application/pdf"
                "epub" -> "application/epub+zip"
                else -> "application/octet-stream"
            }

            // Share intent
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            // Launch chooser on main thread
            withContext(dispatcherProvider.main) {
                context.startActivity(
                    Intent.createChooser(shareIntent, chooserTitle)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }

        }
    }

    override fun fileExists(
        fileUrl: String,
        fileName: String?,
    ): Flow<Boolean> {
        val fileName = fileName(fileUrl, fileName)
        return flow {
            val file = File(appContext.cacheDir, fileName)
            if (file.exists()) {
                emit(true)
            } else {
                val downloaded = try {
                    downloadFile(appContext, fileUrl, fileName)
                } catch (e: Exception) {
                    Timber.e(e, "Failed to download file")
                    null
                }
                println("D_FILE: $fileName => $downloaded")
                emit(downloaded != null)
            }
        }.flowOn(dispatcherProvider.io)
    }

    private fun fileName(fileUrl: String, fileName: String?): String {
        val url = URL(fileUrl)
        val extension = fileUrl.substringAfterLast(".")
        return fileName?.let {
            if (!it.endsWith(".$extension", ignoreCase = true)) "$it.$extension" else it
        } ?: url.file.substringAfterLast("/")
    }

    private suspend fun downloadFile(context: Context, fileUrl: String, fileName: String): File? {
        val file = File(context.cacheDir, fileName)

        if (file.exists()) {
            return file
        }

        val client = OkHttpClient()
        val request = Request.Builder().url(fileUrl).build()
        val response = client.newCall(request).await()

        if (!response.isSuccessful || response.body == null) {
            Toast.makeText(context, "Failed to download file", Toast.LENGTH_SHORT).show()
            return null
        }

        val sink = FileOutputStream(file)
        response.body?.byteStream().use { input ->
            sink.use { output ->
                input?.copyTo(output)
            }
        }

        return file
    }

    suspend fun Call.await(): Response = withContext(dispatcherProvider.io) {
        execute()
    }
}
