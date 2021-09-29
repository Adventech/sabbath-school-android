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
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import app.ss.lessons.data.model.LessonPdf
import com.pspdfkit.configuration.activity.PdfActivityConfiguration
import com.pspdfkit.configuration.activity.ThumbnailBarMode
import com.pspdfkit.configuration.page.PageFitMode
import com.pspdfkit.configuration.settings.SettingsMenuItemType
import com.pspdfkit.configuration.sharing.ShareFeatures
import com.pspdfkit.document.download.DownloadJob
import com.pspdfkit.document.download.DownloadRequest
import com.pspdfkit.ui.DocumentDescriptor
import com.pspdfkit.ui.PdfActivityIntentBuilder
import java.io.File
import java.util.EnumSet

interface PdfReader {
    fun open(activity: AppCompatActivity, pdf: LessonPdf)
    fun open(activity: AppCompatActivity, pdfs: List<LessonPdf>)
}

internal class PdfReaderImpl : PdfReader, DownloadJob.ProgressListenerAdapter() {

    private var downloadJob: DownloadJob? = null

    override fun open(activity: AppCompatActivity, pdf: LessonPdf) {
        val request: DownloadRequest = DownloadRequest.Builder(activity)
            .uri(pdf.src)
            .outputFile(File(activity.getDir(FILE_DIRECTORY, Context.MODE_PRIVATE), "${pdf.id}.pdf"))
            .build()

        downloadJob = DownloadJob.startDownload(request)
        downloadJob?.setProgressListener(object : DownloadJob.ProgressListenerAdapter() {
            override fun onComplete(output: File) {
                downloadJob = null
                val config = PdfActivityConfiguration.Builder(activity)
                    .title(pdf.title)
                    .hidePageLabels()
                    .hideDocumentTitleOverlay()
                    .hidePageNumberOverlay()
                    .hideThumbnailGrid()
                    .disableSearch()
                    .disablePrinting()
                    .disableOutline()
                    .fitMode(PageFitMode.FIT_TO_WIDTH)
                    .animateScrollOnEdgeTaps(true)
                    .setEnabledShareFeatures(EnumSet.noneOf(ShareFeatures::class.java))
                    .setThumbnailBarMode(ThumbnailBarMode.THUMBNAIL_BAR_MODE_NONE)
                    .setSettingsMenuItems(EnumSet.allOf(SettingsMenuItemType::class.java))
                    .build()

                val intent = PdfActivityIntentBuilder.fromDocumentDescriptor(
                    activity,
                    DocumentDescriptor.fromUri(Uri.fromFile(output)),
                )
                    .configuration(config)
                    .build()
                activity.startActivity(intent)
            }
        })
    }

    override fun open(activity: AppCompatActivity, pdfs: List<LessonPdf>) {
        //  val pdf = pdfs.firstOrNull() ?: return
        open(
            activity,
            LessonPdf(
                id = "",
                title = "A Family Tree of God's Love",
                src = "https://sabbath-school-pdf.adventech.io/pdf/en/2021-03-pp/" +
                    "7d328f229ae2532b48433f5a2fd72f2e9261c066084c44bbf967afa6f7bb1b87/7d328f229ae2532b48433f5a2fd72f2e9261c066084c44bbf967afa6f7bb1b87.pdf"
            )
        )
    }

    companion object {
        private const val FILE_DIRECTORY = "ssPdfs"
    }
}
