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

package com.cryart.sabbathschool.lessons.ui.readings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ss.lessons.data.model.LessonPdf
import app.ss.lessons.data.repository.lessons.LessonsRepository
import app.ss.lessons.data.repository.media.SSMediaRepository
import com.cryart.sabbathschool.core.extensions.coroutines.SchedulerProvider
import com.cryart.sabbathschool.core.extensions.intent.lessonIndex
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReadingsViewModel @Inject constructor(
    private val mediaRepository: SSMediaRepository,
    private val lessonsRepository: LessonsRepository,
    private val savedStateHandle: SavedStateHandle,
    schedulerProvider: SchedulerProvider
) : ViewModel() {

    private val _audioAvailable = MutableStateFlow(false)
    val audioAvailableFlow: StateFlow<Boolean> get() = _audioAvailable.asStateFlow()

    private val _videoAvailable = MutableStateFlow(false)
    val videoAvailableFlow: StateFlow<Boolean> get() = _videoAvailable.asStateFlow()

    private val _pdfAvailable = MutableStateFlow(false)
    val pdfAvailableFlow: StateFlow<Boolean> get() = _pdfAvailable.asStateFlow()

    private val _lessonPdfs = MutableStateFlow("" to emptyList<LessonPdf>())
    val lessonPdfsFlow: StateFlow<Pair<String, List<LessonPdf>>> = _lessonPdfs

    val lessonIndex: String? get() = savedStateHandle.lessonIndex

    init {
        viewModelScope.launch(schedulerProvider.default) {
            savedStateHandle.lessonIndex?.let { index ->
                val resource = mediaRepository.getAudio(index)
                _audioAvailable.emit(resource.data.isNullOrEmpty().not())

                val videoResource = mediaRepository.getVideo(index)
                _videoAvailable.emit(videoResource.data.isNullOrEmpty().not())

                val lessonResource = lessonsRepository.getLessonInfo(index)
                val pdfs = lessonResource.data?.pdfs ?: emptyList()
                _lessonPdfs.emit(index to pdfs)
                _pdfAvailable.emit(pdfs.isNotEmpty())
            }
        }
    }
}
