/*
 * Copyright (c) 2021. Adventech <info@adventech.io>
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

package app.ss.pdf

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.Keep
import app.ss.lessons.data.model.LessonPdf
import app.ss.media.model.MediaAvailability
import app.ss.pdf.ui.ARG_MEDIA_AVAILABILITY
import app.ss.pdf.ui.ARG_PDF_FILES
import app.ss.pdf.ui.SSReadPdfActivity
import com.cryart.sabbathschool.core.extensions.coroutines.SchedulerProvider
import com.cryart.sabbathschool.core.misc.SSConstants
import com.cryart.sabbathschool.core.response.Resource
import com.pspdfkit.PSPDFKit
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import java.io.File
import java.util.EnumSet
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Keep
data class LocalFile(val title: String, val uri: Uri)

interface PdfReader {
    fun launchIntent(
        pdfs: List<LessonPdf>,
        lessonIndex: String,
        mediaAvailability: MediaAvailability = MediaAvailability()
    ): Intent

    fun downloadFlow(pdfs: List<LessonPdf>): Flow<Resource<List<LocalFile>>>
}

internal class PdfReaderImpl(
    private val context: Context,
    private val readerPrefs: PdfReaderPrefs,
    private val schedulerProvider: SchedulerProvider
) : PdfReader, DownloadJob.ProgressListenerAdapter() {

    init {
        if (BuildConfig.PSPDFKIT_LICENSE.isNotEmpty()) {
            PSPDFKit.initialize(context, BuildConfig.PSPDFKIT_LICENSE)
        }
    }

    override fun launchIntent(pdfs: List<LessonPdf>, lessonIndex: String, mediaAvailability: MediaAvailability): Intent {
        val excludedAnnotationTypes = ArrayList(EnumSet.allOf(AnnotationType::class.java))
        excludedAnnotationTypes.remove(AnnotationType.HIGHLIGHT)
        excludedAnnotationTypes.remove(AnnotationType.INK)

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
                putExtra(ARG_MEDIA_AVAILABILITY, mediaAvailability)
            }
    }

    override fun downloadFlow(pdfs: List<LessonPdf>): Flow<Resource<List<LocalFile>>> {
        return flow {
            emit(Resource.loading())
            val files = pdfs.mapNotNull { downloadFile(context, it) }
            emit(Resource.success(files))
        }.catch {
            Timber.e(it)
            emit(Resource.error(it))
        }.flowOn(schedulerProvider.default)
    }

    private suspend fun downloadFile(
        context: Context,
        pdf: LessonPdf
    ): LocalFile? = suspendCoroutine { continuation ->
        val request: DownloadRequest = DownloadRequest.Builder(context)
            .uri(pdf.src)
            .outputFile(File(context.getDir(FILE_DIRECTORY, Context.MODE_PRIVATE), "${pdf.id}.pdf"))
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
