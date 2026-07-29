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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ss.pdf.PdfAnnotationJournal
import app.ss.pdf.PdfAnnotationKey
import app.ss.pdf.PdfAnnotationRestorePlan
import app.ss.pdf.PdfAnnotationSaveQueue
import app.ss.pdf.PdfAnnotationSession
import app.ss.pdf.normalized
import app.ss.models.media.MediaAvailability
import com.pspdfkit.annotations.Annotation
import com.pspdfkit.document.PdfDocument
import dagger.hilt.android.lifecycle.HiltViewModel
import io.adventech.blockkit.model.input.PDFAuxAnnotations
import io.adventech.blockkit.model.input.UserInput
import io.adventech.blockkit.model.input.UserInputRequest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ss.foundation.coroutines.flow.stateIn
import ss.libraries.circuit.navigation.PdfScreen
import ss.libraries.pdf.api.LocalFile
import ss.libraries.pdf.api.PdfReader
import ss.resources.api.ResourcesRepository
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
internal class ReadPdfViewModel @Inject constructor(
    private val pdfReader: PdfReader,
    private val resourcesRepository: ResourcesRepository,
    annotationJournal: PdfAnnotationJournal,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val annotationSession = PdfAnnotationSession(annotationJournal)
    private val annotationSaveQueue = PdfAnnotationSaveQueue(viewModelScope) { (key, annotations) ->
        saveRequest(key.documentId, key.pdfId, annotations)
    }

    private val _pdfFiles = MutableStateFlow<List<LocalFile>>(emptyList())
    val pdfsFilesFlow: StateFlow<List<LocalFile>> = _pdfFiles.asStateFlow()

    private val SavedStateHandle.screen: PdfScreen?
        get() = get<PdfScreen>(ARG_PDF_SCREEN)

    val resourceId: String? get() = savedStateHandle.screen?.resourceId
    val documentIndex: String? get() = savedStateHandle.screen?.documentIndex
    val segmentId: String? get() = savedStateHandle.screen?.segmentId

    private val mediaAvailability = MutableStateFlow(MediaAvailability())
    val mediaAvailabilityFlow = mediaAvailability.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val annotationsStateFlow: StateFlow<Map<Int, List<PDFAuxAnnotations>>> =
        flowOf(savedStateHandle.screen?.documentId)
            .filterNotNull()
            .flatMapLatest(resourcesRepository::documentInput)
            .map {
                it.asSequence()
                    .mapNotNull { it as? UserInput.Annotation }
                    .toList()
            }
            .map { input ->
                val pdfIds = savedStateHandle.screen?.pdfs.orEmpty().map { it.id }
                mapAnnotationsByPdfId(pdfIds, input)
            }
            .catch { Timber.e(it) }
            .stateIn(viewModelScope, emptyMap<Int, List<PDFAuxAnnotations>>())

    init {
        checkMediaAvailability()
        downloadFiles()
    }

    private fun checkMediaAvailability() {
        val screen = savedStateHandle.screen ?: return
        val (_, _, documentIndex, resourceIndex, _) = screen
        viewModelScope.launch {
            val audioAvailable = resourcesRepository.audio(resourceIndex, documentIndex).getOrNull().orEmpty().isNotEmpty()
            val videoAvailable = resourcesRepository.video(resourceIndex, documentIndex).getOrNull().orEmpty().isNotEmpty()

            mediaAvailability.update { MediaAvailability(audioAvailable, videoAvailable) }
        }
    }

    private fun downloadFiles() = viewModelScope.launch {
        val pdfs = savedStateHandle.screen?.pdfs ?: return@launch
        val result = pdfReader.downloadFiles(pdfs)
        val files = result.getOrDefault(emptyList())
        _pdfFiles.update { files }
    }

    fun saveAnnotations(document: PdfDocument, docIndex: Int) {
        captureAnnotations(document, docIndex, flush = false)
    }

    fun flushAnnotations(document: PdfDocument, docIndex: Int) {
        captureAnnotations(document, docIndex, flush = true)
    }

    private fun captureAnnotations(
        document: PdfDocument,
        docIndex: Int,
        flush: Boolean,
    ) {
        val annotations = try {
            document.annotations().toSync()
        } catch (error: RuntimeException) {
            Timber.e(error, "Unable to serialize a complete PDF annotation snapshot")
            return
        }
        saveAnnotations(annotations, docIndex, flush)
    }

    fun restorePlan(
        docIndex: Int,
        remoteAnnotations: List<PDFAuxAnnotations>?,
    ): PdfAnnotationRestorePlan? {
        val pdfs = savedStateHandle.screen?.pdfs ?: return null
        val documentId = savedStateHandle.screen?.documentId ?: return null
        val pdfId = pdfs.getOrNull(docIndex)?.id ?: return null
        val key = PdfAnnotationKey(documentId, pdfId)
        val plan = if (remoteAnnotations == null) {
            annotationSession.resolveAbsent(key)
        } else {
            annotationSession.resolve(key, remoteAnnotations)
        }
        return plan?.also {
            if (plan.replayRequired) annotationSaveQueue.flush(key, plan.annotations)
        }
    }

    private fun saveAnnotations(
        annotations: List<PDFAuxAnnotations>,
        docIndex: Int,
        flush: Boolean,
    ) {
        val pdfs = savedStateHandle.screen?.pdfs ?: return
        val documentId = savedStateHandle.screen?.documentId ?: return
        val pdfId = pdfs.getOrNull(docIndex)?.id ?: return
        val key = PdfAnnotationKey(documentId, pdfId)

        if (!annotationSession.record(key, annotations)) {
            Timber.e("Unable to durably journal PDF annotations")
        }
        if (flush) {
            annotationSaveQueue.flush(key, annotations)
        } else {
            annotationSaveQueue.enqueue(key, annotations)
        }
    }

    private fun saveRequest(
        documentId: String,
        pdfId: String,
        annotations: List<PDFAuxAnnotations>,
    ) {
        val userInput = UserInputRequest.Annotation(
            blockId = pdfId,
            pdfId = pdfId,
            data = annotations.normalized(),
        )

        resourcesRepository.saveDocumentInput(documentId, userInput)
    }
}

internal fun mapAnnotationsByPdfId(
    pdfIds: List<String>,
    inputs: List<UserInput.Annotation>,
): Map<Int, List<PDFAuxAnnotations>> {
    val inputsByPdfId = inputs.groupBy { it.pdfId }
    return pdfIds.mapIndexedNotNull { index, pdfId ->
        inputsByPdfId[pdfId]?.let { matches -> index to matches.flatMap { it.data } }
    }.toMap()
}

internal fun List<Annotation>.toSync(): List<PDFAuxAnnotations> {
    val groupedAnnotations = groupBy { it.pageIndex }
    return groupedAnnotations.keys.mapNotNull { pageIndex ->
        val list = groupedAnnotations[pageIndex] ?: return@mapNotNull null
        val annotations = list.map { annotation ->
            annotation.toInstantJson().also { json ->
                check(json != "null") { "PDF annotation did not serialize" }
            }
        }
        PDFAuxAnnotations(pageIndex, annotations)
    }.normalized()
}

fun PdfDocument.annotations(): List<Annotation> {
    val allAnnotations = mutableListOf<Annotation>()
    for (i in 0 until pageCount) {
        val annotations = annotationProvider.getAnnotations(i)
        allAnnotations.addAll(annotations)
    }
    return allAnnotations
}
