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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ss.lessons.data.model.LessonPdf
import app.ss.lessons.data.model.PdfAnnotations
import app.ss.lessons.data.repository.lessons.LessonsRepository
import app.ss.media.model.MediaAvailability
import app.ss.pdf.LocalFile
import app.ss.pdf.PdfReader
import com.cryart.sabbathschool.core.extensions.coroutines.SchedulerProvider
import com.cryart.sabbathschool.core.extensions.coroutines.flow.stateIn
import com.cryart.sabbathschool.core.extensions.intent.lessonIndex
import com.cryart.sabbathschool.core.response.Resource
import com.pspdfkit.annotations.Annotation
import com.pspdfkit.annotations.AnnotationProvider
import com.pspdfkit.document.PdfDocument
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ReadPdfViewModel @Inject constructor(
    pdfReader: PdfReader,
    private val lessonsRepository: LessonsRepository,
    private val savedStateHandle: SavedStateHandle,
    private val schedulerProvider: SchedulerProvider
) : ViewModel() {

    val pdfsFilesFlow: StateFlow<Resource<List<LocalFile>>> = pdfReader.downloadFlow(savedStateHandle.pdfs)
        .stateIn(viewModelScope, Resource.loading())

    private val SavedStateHandle.pdfs: List<LessonPdf>
        get() = get<ArrayList<LessonPdf>>(ARG_PDF_FILES) ?: emptyList()

    val mediaActivity: MediaAvailability
        get() = savedStateHandle.get<MediaAvailability>(
            ARG_MEDIA_AVAILABILITY
        ) ?: MediaAvailability()

    val lessonIndex: String? get() = savedStateHandle.lessonIndex

    private val _annotationsUpdate = MutableSharedFlow<Int>()
    val annotationsUpdateFlow: SharedFlow<Int> = _annotationsUpdate

    private var currDocIndex = -1
    private val annotationUpdatedListener = SSAnnotationUpdatedListener {
       // viewModelScope.launch { _annotationsUpdate.emit(currDocIndex) }
    }

    private var savedAnnotations: List<PdfAnnotations> = emptyList()

    init {
        viewModelScope.launch {
            val lessonIndex = lessonIndex ?: return@launch
            savedStateHandle.pdfs.forEachIndexed { index, pdf ->
                lessonsRepository.getAnnotations(lessonIndex, pdf.id).collect { resource ->
                    val syncAnnotations = resource.data ?: return@collect
                    if (syncAnnotations == savedAnnotations) return@collect

                    savedAnnotations = syncAnnotations
                }
            }
        }
    }

    fun onDocumentLoaded(document: PdfDocument, docIndex: Int) {
        val index = lessonIndex ?: return
        val pdfId = savedStateHandle.pdfs.getOrNull(docIndex)?.id ?: return
        currDocIndex = docIndex
        document.annotationProvider.addOnAnnotationUpdatedListener(annotationUpdatedListener)

        //   delay(2000) // wait for pdf to load

        /*lessonsRepository.getAnnotations(index, pdfId).collect { resource ->
            val syncAnnotations = resource.data ?: return@collect
            if (syncAnnotations == savedAnnotations) return@collect

            savedAnnotations = syncAnnotations

            with(document.annotationProvider) {

                removeOnAnnotationUpdatedListener(annotationUpdatedListener)

                withContext(schedulerProvider.main) {
                    if (syncAnnotations.isNotEmpty()) {
                        document.annotations()
                            .forEach { removeAnnotationFromPage(it) }

                        delay(2000)

                        syncAnnotations
                            .flatMap { it.annotations }
                            .forEach { createAnnotationFromInstantJson(it) }
                    }
                }

                addOnAnnotationUpdatedListener(annotationUpdatedListener)
            }
        }*/
    }

    fun saveAnnotations(document: PdfDocument, docIndex: Int) {
        val lessonIndex = lessonIndex ?: return
        val pdfId = savedStateHandle.pdfs.getOrNull(docIndex)?.id ?: return

        val syncAnnotations = document.annotations().toSync()

        this.savedAnnotations = syncAnnotations

        lessonsRepository.saveAnnotations(lessonIndex, pdfId, syncAnnotations)
    }

    private fun PdfDocument.annotations(): List<Annotation> {
        val allAnnotations = mutableListOf<Annotation>()
        for (i in 0 until pageCount) {
            val annotations = annotationProvider.getAnnotations(i)
            allAnnotations.addAll(annotations)
        }

        return allAnnotations
    }

    private fun List<Annotation>.toSync(): List<PdfAnnotations> {
        val groupedAnnotations = groupBy { it.pageIndex }
        return groupedAnnotations.keys.mapNotNull { pageIndex ->
            val list = groupedAnnotations[pageIndex] ?: return@mapNotNull null
            val annotations = list.map { it.toInstantJson() }.filter(::invalidInstantJson)
            PdfAnnotations(pageIndex, annotations)
        }
    }

    private fun invalidInstantJson(json: String) = json != "null"
}

internal class SSAnnotationUpdatedListener(
    private val onUpdate: () -> Unit
) : AnnotationProvider.OnAnnotationUpdatedListener {
    override fun onAnnotationCreated(a: Annotation) = onUpdate()
    override fun onAnnotationUpdated(a: Annotation) = onUpdate()
    override fun onAnnotationRemoved(a: Annotation) = onUpdate()
    override fun onAnnotationZOrderChanged(p0: Int, p1: MutableList<Annotation>, p2: MutableList<Annotation>) {}
}
