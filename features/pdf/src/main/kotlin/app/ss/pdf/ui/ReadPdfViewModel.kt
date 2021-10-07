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
import com.cryart.sabbathschool.core.extensions.coroutines.flow.stateIn
import com.cryart.sabbathschool.core.extensions.intent.lessonIndex
import com.cryart.sabbathschool.core.response.Resource
import com.pspdfkit.annotations.Annotation
import com.pspdfkit.annotations.AnnotationProvider
import com.pspdfkit.document.PdfDocument
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Completable
import io.reactivex.Single
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ReadPdfViewModel @Inject constructor(
    pdfReader: PdfReader,
    private val lessonsRepository: LessonsRepository,
    private val savedStateHandle: SavedStateHandle,
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

    private val currentDocAnnotations = mutableListOf<Annotation>()
    private var syncAnnotationsJob: Job? = null

    fun onDocumentLoaded(document: PdfDocument, docIndex: Int) {
        currentDocAnnotations.clear()

        val index = lessonIndex ?: return
        val pdfId = savedStateHandle.pdfs.getOrNull(docIndex)?.id ?: return

        val annotationUpdatedListener = SSAnnotationUpdatedListener { annotation ->
            currentDocAnnotations.removeAll { it.uuid == annotation.uuid }
            if (annotation.isAttached) {
                currentDocAnnotations.add(annotation)
            }

            val groupedAnnotations = currentDocAnnotations.groupBy { it.pageIndex }
            val allAnnotations = groupedAnnotations.keys.mapNotNull { pageIndex ->
                val list = groupedAnnotations[pageIndex] ?: return@mapNotNull null
                val annotations = list.map { it.toInstantJson() }.filter(::invalidInstantJson)
                PdfAnnotations(pageIndex, annotations)
            }

            lessonsRepository.saveAnnotations(index, pdfId, allAnnotations)
        }
        document.annotationProvider.addOnAnnotationUpdatedListener(annotationUpdatedListener)

        for (i in 0 until document.pageCount) {
            val annotations = document.annotationProvider.getAnnotations(i)
            currentDocAnnotations.addAll(annotations)
        }

        syncAnnotationsJob?.cancel()
        syncAnnotationsJob = viewModelScope.launch {
            lessonsRepository.getAnnotations(index, pdfId).collect { resource ->
                val annotations = resource.data ?: return@collect

                if (annotations.isEmpty()) return@collect

                try {
                    with(document.annotationProvider) {
                        removeOnAnnotationUpdatedListener(annotationUpdatedListener)
                        val existing = currentDocAnnotations.toList()
                        currentDocAnnotations.clear()
                        existing.forEach { removeAnnotationFromPageAsync(it).await(viewModelScope) }

                        annotations
                            .flatMap { it.annotations }
                            .map { createAnnotationFromInstantJsonAsync(it).await(viewModelScope) }
                            .forEach { currentDocAnnotations.add(it) }

                        addOnAnnotationUpdatedListener(annotationUpdatedListener)
                    }
                } catch (ex: Exception) {
                    Timber.e(ex)
                }
            }
        }
    }
}

private fun invalidInstantJson(json: String) = json != "null"
private suspend fun Completable.await(scope: CoroutineScope) = withContext(scope.coroutineContext) { blockingAwait() }
private suspend fun <T> Single<T>.await(scope: CoroutineScope): T = withContext(scope.coroutineContext) { blockingGet() }

internal class SSAnnotationUpdatedListener(
    private val onUpdate: (Annotation) -> Unit
) : AnnotationProvider.OnAnnotationUpdatedListener {
    override fun onAnnotationCreated(a: Annotation) = onUpdate(a)
    override fun onAnnotationUpdated(a: Annotation) = onUpdate(a)
    override fun onAnnotationRemoved(a: Annotation) = onUpdate(a)
    override fun onAnnotationZOrderChanged(p0: Int, p1: MutableList<Annotation>, p2: MutableList<Annotation>) {}
}
