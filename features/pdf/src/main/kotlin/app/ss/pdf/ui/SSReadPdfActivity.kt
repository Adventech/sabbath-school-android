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

package app.ss.pdf.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import app.ss.lessons.data.model.PdfAnnotations
import app.ss.media.playback.ui.nowPlaying.showNowPlaying
import app.ss.media.playback.ui.video.showVideoList
import app.ss.pdf.PdfReaderPrefs
import app.ss.pdf.R
import com.cryart.sabbathschool.core.extensions.coroutines.flow.collectIn
import com.cryart.sabbathschool.core.extensions.view.tint
import com.pspdfkit.document.DocumentSource
import com.pspdfkit.document.PdfDocument
import com.pspdfkit.ui.DocumentDescriptor
import com.pspdfkit.ui.PdfActivity
import com.pspdfkit.ui.tabs.PdfTabBarCloseMode
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SSReadPdfActivity : PdfActivity() {

    @Inject
    lateinit var readerPrefs: PdfReaderPrefs

    private val viewModel by viewModels<ReadPdfViewModel>()

    private var loadedDocuments: List<DocumentDescriptor> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initUi()

        collectData()
    }

    override fun onGenerateMenuItemIds(menuItems: MutableList<Int>): MutableList<Int> {
        return menuItems.also {
            val media = viewModel.mediaActivity
            if (media.video) {
                it.add(0, ID_VIDEO)
            }
            if (media.audio) {
                it.add(0, ID_AUDIO)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)

        menu?.findItem(ID_AUDIO)?.custom(R.string.ss_media_audio, R.drawable.ic_audio_icon)
        menu?.findItem(ID_VIDEO)?.custom(R.string.ss_media_video, R.drawable.ic_video_icon)

        return true
    }

    private fun MenuItem.custom(
        @StringRes titleRes: Int,
        @DrawableRes iconRes: Int
    ) {
        title = getString(titleRes)
        setIcon(iconRes)
        setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        icon?.tint(ContextCompat.getColor(this@SSReadPdfActivity, R.color.ss_icon_tint))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> true.also { finish() }
            ID_AUDIO -> true.also { supportFragmentManager.showNowPlaying(viewModel.lessonIndex) }
            ID_VIDEO -> true.also {
                viewModel.lessonIndex?.let {
                    supportFragmentManager.showVideoList(it)
                }
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initUi() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        pspdfKitViews.tabBar?.setCloseMode(PdfTabBarCloseMode.CLOSE_DISABLED)
        (pspdfKitViews.emptyView as? ViewGroup)?.apply {
            removeAllViews()
            addView(ProgressBar(this@SSReadPdfActivity))
        }
    }

    private fun collectData() {
        viewModel.pdfsFilesFlow.collectIn(this) { resource ->
            if (documentCoordinator.documents.isNotEmpty()) return@collectIn

            loadedDocuments = resource.data?.map { file ->
                DocumentDescriptor.fromDocumentSource(DocumentSource(file.uri)).apply {
                    setTitle(file.title)
                }
            } ?: return@collectIn

            if (loadedDocuments.isEmpty()) return@collectIn

            loadedDocuments.forEach { documentCoordinator.addDocument(it) }
            documentCoordinator.setVisibleDocument(loadedDocuments.first())
        }

        viewModel.annotationsUpdateFlow.collectIn(this) { docIndex ->
            val document = documentCoordinator.documents.getOrNull(docIndex)?.document ?: return@collectIn
            val annotations = viewModel.annotationsMap[docIndex] ?: return@collectIn

            loadAnnotations(document, annotations)
        }
    }

    override fun onDocumentLoaded(document: PdfDocument) {
        val index = loadedDocuments.indexOfFirst { it.uid == documentCoordinator.visibleDocument?.uid }
        if (index >= 0) {
            val annotations = viewModel.annotationsMap[index] ?: return
            loadAnnotations(document, annotations)
        }
    }

    private fun loadAnnotations(document: PdfDocument, annotations: List<PdfAnnotations>) {
        if (annotations.isEmpty()) return

        with(document.annotationProvider) {
            document.annotations()
                .forEach { removeAnnotationFromPage(it) }

            annotations
                .flatMap { it.annotations }
                .forEach { createAnnotationFromInstantJson(it) }
        }
    }

    override fun onDestroy() {
        val documents = loadedDocuments.mapNotNull { it.document }
        documents.forEachIndexed { index, pdfDocument ->
            viewModel.saveAnnotations(pdfDocument, index)
        }
        readerPrefs.saveConfiguration(configuration.configuration)
        super.onDestroy()
    }

    companion object {
        private const val ID_AUDIO = 23
        private const val ID_VIDEO = 24
    }
}

internal const val ARG_PDF_FILES = "ss_arg_pdf_files"
internal const val ARG_MEDIA_AVAILABILITY = "aa_arg_media_availability"
