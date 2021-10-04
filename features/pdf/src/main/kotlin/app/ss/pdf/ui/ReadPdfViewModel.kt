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
import app.ss.media.model.MediaAvailability
import app.ss.pdf.LocalFile
import app.ss.pdf.PdfReader
import com.cryart.sabbathschool.core.extensions.coroutines.flow.stateIn
import com.cryart.sabbathschool.core.extensions.intent.lessonIndex
import com.cryart.sabbathschool.core.response.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ReadPdfViewModel @Inject constructor(
    pdfReader: PdfReader,
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
}
