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

package app.ss.pdf

import android.content.Context
import android.content.Intent
import android.net.Uri
import app.ss.models.LessonPdf
import app.ss.pdf.ui.ARG_PDF_FILES
import app.ss.pdf.ui.SSReadPdfActivity
import com.pspdfkit.annotations.AnnotationType
import com.pspdfkit.configuration.activity.PdfActivityConfiguration
import com.pspdfkit.configuration.activity.TabBarHidingMode
import com.pspdfkit.configuration.activity.ThumbnailBarMode
import com.pspdfkit.configuration.page.PageFitMode
import com.pspdfkit.configuration.settings.SettingsMenuItemType
import com.pspdfkit.configuration.sharing.ShareFeatures
import com.pspdfkit.document.download.DownloadJob
import com.pspdfkit.document.download.DownloadRequest
import com.pspdfkit.ui.PdfActivityIntentBuilder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
import ss.foundation.coroutines.DispatcherProvider
import ss.lessons.api.PdfReader
import ss.lessons.model.LocalFile
import ss.misc.SSConstants
import timber.log.Timber
import java.io.File
import java.util.EnumSet
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
internal class PdfReaderImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val readerPrefs: PdfReaderPrefs,
    private val dispatcherProvider: DispatcherProvider
) : PdfReader, DownloadJob.ProgressListenerAdapter() {

    private val allowedAnnotations = listOf(
        AnnotationType.HIGHLIGHT,
        AnnotationType.INK,
        AnnotationType.NOTE,
        AnnotationType.WATERMARK,
        AnnotationType.STRIKEOUT,
        AnnotationType.FREETEXT
    )

    override fun launchIntent(pdfs: List<LessonPdf>, lessonIndex: String): Intent {
        val excludedAnnotationTypes = ArrayList(EnumSet.allOf(AnnotationType::class.java))
        allowedAnnotations.forEach { excludedAnnotationTypes.remove(it) }

        val config = PdfActivityConfiguration.Builder(context)
            .hidePageLabels()
            .hideDocumentTitleOverlay()
            .disableDocumentInfoView()
            .hidePageNumberOverlay()
            .hideThumbnailGrid()
            .disableSearch()
            .disablePrinting()
            .disableOutline()
            .fitMode(PageFitMode.FIT_TO_WIDTH)
            .animateScrollOnEdgeTaps(true)
            .excludedAnnotationTypes(excludedAnnotationTypes)
            .setEnabledShareFeatures(EnumSet.noneOf(ShareFeatures::class.java))
            .setThumbnailBarMode(ThumbnailBarMode.THUMBNAIL_BAR_MODE_NONE)
            .setSettingsMenuItems(EnumSet.allOf(SettingsMenuItemType::class.java))
            .setTabBarHidingMode(TabBarHidingMode.AUTOMATIC)
            .scrollMode(readerPrefs.scrollMode())
            .scrollDirection(readerPrefs.scrollDirection())
            .layoutMode(readerPrefs.pageLayoutMode())
            .themeMode(readerPrefs.themeMode())
            .build()

        return PdfActivityIntentBuilder.emptyActivity(context)
            .configuration(config)
            .activityClass(SSReadPdfActivity::class.java)
            .build()
            .apply {
                putParcelableArrayListExtra(ARG_PDF_FILES, ArrayList(pdfs))
                putExtra(SSConstants.SS_LESSON_INDEX_EXTRA, lessonIndex)
            }
    }

    override suspend fun downloadFiles(pdfs: List<LessonPdf>): Result<List<LocalFile>> {
        return try {
            val files = pdfs.mapNotNull {
                withContext(dispatcherProvider.io) { downloadFile(context, it) }
            }
            Result.success(files)
        } catch (ex: Throwable) {
            Timber.e(ex)
            Result.failure(ex)
        }
    }

    override fun isDownloaded(pdf: LessonPdf): Boolean {
        return File(context.getDir(FILE_DIRECTORY, Context.MODE_PRIVATE), "${pdf.id}.pdf").exists()
    }

    private suspend fun downloadFile(
        context: Context,
        pdf: LessonPdf
    ): LocalFile? = suspendCoroutine { continuation ->
        val outputFile = File(context.getDir(FILE_DIRECTORY, Context.MODE_PRIVATE), "${pdf.id}.pdf")
        if (outputFile.exists()) {
            continuation.resume(LocalFile(pdf.title, Uri.fromFile(outputFile)))
            return@suspendCoroutine
        }

        val request: DownloadRequest = DownloadRequest.Builder(context)
            .uri(pdf.src)
            .outputFile(outputFile)
            .build()

        val downloadJob = DownloadJob.startDownload(request)
        downloadJob?.setProgressListener(object : DownloadJob.ProgressListenerAdapter() {
            override fun onComplete(output: File) {
                continuation.resume(LocalFile(pdf.title, Uri.fromFile(output)))
            }

            override fun onError(exception: Throwable) {
                Timber.e(exception)
                continuation.resume(null)
            }
        })
    }

    companion object {
        private const val FILE_DIRECTORY = "ssPdfs"
    }
}
