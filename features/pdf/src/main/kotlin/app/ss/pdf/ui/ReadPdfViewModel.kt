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
import app.ss.lessons.data.repository.media.MediaRepository
import app.ss.models.media.MediaAvailability
import com.pspdfkit.annotations.Annotation
import com.pspdfkit.document.PdfDocument
import dagger.hilt.android.lifecycle.HiltViewModel
import io.adventech.blockkit.model.input.PDFAuxAnnotations
import io.adventech.blockkit.model.input.UserInput
import io.adventech.blockkit.model.input.UserInputRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
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
class ReadPdfViewModel @Inject constructor(
    private val pdfReader: PdfReader,
    private val mediaRepository: MediaRepository,
    private val resourcesRepository: ResourcesRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _pdfFiles = MutableStateFlow<List<LocalFile>>(emptyList())
    val pdfsFilesFlow: StateFlow<List<LocalFile>> = _pdfFiles.asStateFlow()

    private val SavedStateHandle.screen: PdfScreen?
        get() = get<PdfScreen>(ARG_PDF_SCREEN)

    val documentId: String? get() = savedStateHandle.screen?.documentId

    private val mediaAvailability = MutableStateFlow(MediaAvailability())
    val mediaAvailabilityFlow = mediaAvailability.asStateFlow()

    val annotationsStateFlow: StateFlow<Map<Int, List<PDFAuxAnnotations>>> =
        flowOf(documentId)
            .filterNotNull()
            .flatMapLatest(resourcesRepository::documentInput)
            .map {
                it.asSequence()
                    .mapNotNull { it as? UserInput.Annotation }
                    .toList()
            }
            .map { input ->
                val pdfs = savedStateHandle.screen?.pdfs.orEmpty()
                pdfs.mapIndexed { index, pdf ->
                    index to input.filter { it.pdfId == pdf.id }.flatMap { it.data }
                }.toMap()
            }
            .catch { Timber.e(it) }
            .stateIn(viewModelScope, emptyMap<Int, List<PDFAuxAnnotations>>())

    init {
        //            checkMediaAvailability(lessonIndex)
        downloadFiles()
    }

    private fun checkMediaAvailability(lessonIndex: String) {
        viewModelScope.launch {
            // Read media availability from Resource repository
            combine(mediaRepository.getAudio(lessonIndex), mediaRepository.getVideo(lessonIndex)) { audio, video ->
                audio.isNotEmpty() to video.isNotEmpty()
            }
                .catch { Timber.e(it) }
                .collect { (audioAvailable, videoAvailable) ->
                    mediaAvailability.update { MediaAvailability(audioAvailable, videoAvailable) }
                }
        }
    }

    private fun downloadFiles() = viewModelScope.launch {
        val pdfs = savedStateHandle.screen?.pdfs ?: return@launch
        val result = pdfReader.downloadFiles(pdfs)
        val files = result.getOrDefault(emptyList())
        _pdfFiles.update { files }
    }

    fun saveAnnotations(document: PdfDocument, docIndex: Int) {
        val pdfs = savedStateHandle.screen?.pdfs ?: return
        val documentId = savedStateHandle.screen?.documentId ?: return
        val pdfId = pdfs.getOrNull(docIndex)?.id ?: return

        val syncAnnotations = document.annotations().toSync()

        val userInput = UserInputRequest.Annotation(
            blockId = pdfId,
            pdfId = pdfId,
            data = syncAnnotations
        )

       resourcesRepository.saveDocumentInput(documentId, userInput)
    }

    private fun List<Annotation>.toSync(): List<PDFAuxAnnotations> {
        val groupedAnnotations = groupBy { it.pageIndex }
        return groupedAnnotations.keys.mapNotNull { pageIndex ->
            val list = groupedAnnotations[pageIndex] ?: return@mapNotNull null
            val annotations = list.map { it.toInstantJson() }.filter(::invalidInstantJson)
            PDFAuxAnnotations(pageIndex, annotations)
        }
    }

    private fun invalidInstantJson(json: String) = json != "null"
}

fun PdfDocument.annotations(): List<Annotation> {
    val allAnnotations = mutableListOf<Annotation>()
    for (i in 0 until pageCount) {
        val annotations = annotationProvider.getAnnotations(i)
        allAnnotations.addAll(annotations)
    }
    return allAnnotations
}
