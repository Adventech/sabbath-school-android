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
import app.ss.media.playback.ui.nowPlaying.showNowPlaying
import app.ss.media.playback.ui.video.showVideoList
import app.ss.pdf.PdfReaderPrefs
import app.ss.pdf.R
import app.ss.pdf.PdfAnnotationRestorePlan
import app.ss.pdf.normalized
import com.cryart.sabbathschool.core.extensions.view.tint
import com.pspdfkit.annotations.Annotation
import com.pspdfkit.annotations.AnnotationProvider
import com.pspdfkit.document.DocumentSource
import com.pspdfkit.document.PdfDocument
import com.pspdfkit.ui.DocumentDescriptor
import com.pspdfkit.ui.PdfActivity
import com.pspdfkit.ui.tabs.PdfTabBarCloseMode
import dagger.hilt.android.AndroidEntryPoint
import io.adventech.blockkit.model.input.PDFAuxAnnotations
import ss.foundation.coroutines.flow.collectIn
import timber.log.Timber
import java.util.IdentityHashMap
import javax.inject.Inject
import app.ss.translations.R as L10n
import ss.libraries.media.resources.R as MediaR

@AndroidEntryPoint
class SSReadPdfActivity : PdfActivity() {

    @Inject
    lateinit var readerPrefs: PdfReaderPrefs

    private val viewModel by viewModels<ReadPdfViewModel>()

    private var loadedDocuments: List<DocumentDescriptor> = emptyList()
    private val annotationListeners = IdentityHashMap<PdfDocument, AnnotationProvider.OnAnnotationUpdatedListener>()
    private val restoringDocuments = java.util.Collections.newSetFromMap(IdentityHashMap<PdfDocument, Boolean>())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initUi()

        collectData()
    }

    override fun onGenerateMenuItemIds(menuItems: MutableList<Int>): MutableList<Int> {
        return menuItems.also {
            val media = viewModel.mediaAvailabilityFlow.value
            if (media.video) {
                it.add(0, ID_VIDEO)
            }
            if (media.audio) {
                it.add(0, ID_AUDIO)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)

        menu.findItem(ID_AUDIO)?.custom(L10n.string.ss_media_audio, MediaR.drawable.ic_audio_icon)
        menu.findItem(ID_VIDEO)?.custom(L10n.string.ss_media_video, MediaR.drawable.ic_video_icon)

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
            ID_AUDIO -> true.also {
                supportFragmentManager.showNowPlaying(viewModel.resourceId, viewModel.segmentId)
            }
            ID_VIDEO -> true.also {
                viewModel.documentIndex?.let {
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
        viewModel.pdfsFilesFlow.collectIn(this) { files ->
            if (documentCoordinator.documents.isNotEmpty()) return@collectIn

            loadedDocuments = files.map { file ->
                DocumentDescriptor.fromDocumentSource(DocumentSource(file.uri)).apply {
                    setTitle(file.title)
                }
            }

            if (loadedDocuments.isEmpty()) return@collectIn

            loadedDocuments.forEach { documentCoordinator.addDocument(it) }
            documentCoordinator.setVisibleDocument(loadedDocuments.first())
        }

        viewModel.annotationsStateFlow.collectIn(this) { annotations ->
            annotations.forEach { (index, annotations) ->
                val document = documentCoordinator.documents.getOrNull(index)?.document ?: return@forEach
                viewModel.restorePlan(index, annotations)?.let { plan ->
                    loadAnnotations(document, plan)
                }
            }
        }

        viewModel.mediaAvailabilityFlow.collectIn(this) { invalidateOptionsMenu() }
    }

    override fun onDocumentLoaded(document: PdfDocument) {
        val index = loadedDocuments.indexOfFirst { descriptor ->
            descriptor.document === document || descriptor.uid == document.uid
        }
        if (index >= 0) {
            registerAnnotationListener(document, index)
            val annotations = viewModel.annotationsStateFlow.value[index]
            viewModel.restorePlan(index, annotations)?.let { plan ->
                loadAnnotations(document, plan)
            }
        }
    }

    private fun registerAnnotationListener(document: PdfDocument, index: Int) {
        if (annotationListeners.containsKey(document)) return
        val listener = object : AnnotationProvider.OnAnnotationUpdatedListener {
            override fun onAnnotationCreated(annotation: Annotation) = annotationsChanged(document, index)
            override fun onAnnotationUpdated(annotation: Annotation) = annotationsChanged(document, index)
            override fun onAnnotationRemoved(annotation: Annotation) = annotationsChanged(document, index)

            override fun onAnnotationZOrderChanged(
                pageIndex: Int,
                oldOrder: List<Annotation>,
                newOrder: List<Annotation>,
            ) = annotationsChanged(document, index)
        }
        document.annotationProvider.addOnAnnotationUpdatedListener(listener)
        annotationListeners[document] = listener
    }

    private fun annotationsChanged(document: PdfDocument, index: Int) {
        if (document in restoringDocuments) return
        viewModel.saveAnnotations(document, index)
    }

    private fun loadAnnotations(document: PdfDocument, plan: PdfAnnotationRestorePlan) {
        restoringDocuments += document
        val restored = try {
            replaceAnnotationsSafely(
                current = { document.annotations().toSync() },
                clear = {
                    document.annotations().forEach(document.annotationProvider::removeAnnotationFromPage)
                },
                apply = { annotations ->
                    annotations
                        .flatMap { it.annotations }
                        .forEach { json ->
                            checkNotNull(document.annotationProvider.createAnnotationFromInstantJson(json))
                        }
                },
                candidate = plan.annotations,
                fallback = plan.fallback,
            )
        } finally {
            restoringDocuments -= document
        }
        if (!restored) Timber.e("Rejected malformed PDF annotation payload and restored known-good state")
    }

    override fun onStop() {
        loadedDocuments.map { it.document }.forEachIndexedPresent { index, pdfDocument ->
            viewModel.flushAnnotations(pdfDocument, index)
        }
        readerPrefs.saveConfiguration(configuration.configuration)
        super.onStop()
    }

    override fun onDestroy() {
        annotationListeners.forEach { (document, listener) ->
            document.annotationProvider.removeOnAnnotationUpdatedListener(listener)
        }
        annotationListeners.clear()
        super.onDestroy()
    }

    companion object {
        private const val ID_AUDIO = 23
        private const val ID_VIDEO = 24
    }
}

internal const val ARG_PDF_SCREEN = "as_arg_pdf_screen"

internal fun <T : Any> List<T?>.forEachIndexedPresent(action: (Int, T) -> Unit) {
    forEachIndexed { index, value ->
        if (value != null) action(index, value)
    }
}

internal fun replaceAnnotationsSafely(
    current: () -> List<PDFAuxAnnotations>,
    clear: () -> Unit,
    apply: (List<PDFAuxAnnotations>) -> Unit,
    candidate: List<PDFAuxAnnotations>,
    fallback: List<PDFAuxAnnotations>?,
): Boolean {
    val before = try {
        current().normalized()
    } catch (_: Exception) {
        return false
    }
    val replacement = candidate.normalized()
    if (before == replacement) return true

    return try {
        clear()
        apply(replacement)
        true
    } catch (_: Exception) {
        val preferredRecovery = fallback?.normalized() ?: before
        val recovered = runCatching {
            clear()
            apply(preferredRecovery)
        }.isSuccess
        if (!recovered && preferredRecovery != before) {
            runCatching {
                clear()
                apply(before)
            }
        }
        false
    }
}
