/*
 * Copyright (c) 2024. Adventech <info@adventech.io>
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

package app.ss.readings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ss.lessons.data.repository.lessons.LessonsRepository
import app.ss.lessons.data.repository.media.MediaRepository
import app.ss.lessons.data.repository.user.UserDataRepository
import app.ss.models.LessonPdf
import app.ss.models.PublishingInfo
import app.ss.models.SSReadComments
import app.ss.models.SSReadHighlights
import app.ss.models.media.MediaAvailability
import com.cryart.sabbathschool.core.extensions.intent.lessonIndex
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import ss.foundation.coroutines.flow.stateIn
import ss.lessons.api.repository.LessonsRepositoryV2
import ss.lessons.api.repository.QuarterliesRepository
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ReadingsViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
    private val lessonsRepositoryV2: LessonsRepositoryV2,
    private val userDataRepository: UserDataRepository,
    private val savedStateHandle: SavedStateHandle,
    lessonsRepository: LessonsRepository,
    quarterliesRepository: QuarterliesRepository,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val mediaAvailability =
        flowOf(lessonIndex)
            .mapNotNull { it }
            .flatMapLatest {
                combine(mediaRepository.getAudio(it), mediaRepository.getVideo(it)) { audio, video ->
                    MediaAvailability(audio.isNotEmpty(), video.isNotEmpty())
                }
            }.catch {
                Timber.e(it)
                emit(MediaAvailability(audio = false, video = false))
            }.stateIn(viewModelScope, MediaAvailability(audio = false, video = false))

    private val _pdfAvailable = MutableStateFlow(false)
    val pdfAvailableFlow: StateFlow<Boolean> get() = _pdfAvailable.asStateFlow()

    private val _lessonPdfs = MutableStateFlow("" to emptyList<LessonPdf>())
    val lessonPdfsFlow: StateFlow<Pair<String, List<LessonPdf>>> = _lessonPdfs

    val publishingInfo: StateFlow<PublishingInfo?> = quarterliesRepository.getPublishingInfo()
        .mapNotNull { it.getOrNull() }
        .distinctUntilChanged()
        .stateIn(viewModelScope, null)

    val lessonIndex: String? get() = savedStateHandle.lessonIndex

    init {
        lessonIndex?.let { index ->
            viewModelScope.launch {
                lessonsRepositoryV2.getLessonInfo(index)
                    .mapNotNull { it.getOrNull() }
                    .catch { Timber.e(it) }
                    .collect { lessonInfo ->
                        val pdfs = lessonInfo.pdfs
                        _lessonPdfs.emit(index to pdfs)
                        _pdfAvailable.emit(pdfs.isNotEmpty())
                    }
            }
        }

        lessonsRepository.checkReaderArtifact()
    }

    internal fun readUserContentFlow(
        readIndex: String,
        defaultContent: ReadUserContent?
    ): StateFlow<ReadUserContent> {
        val initial = defaultContent ?: ReadUserContent(
            readIndex,
            SSReadComments(readIndex, emptyList()),
            SSReadHighlights(readIndex)
        )
        return combine(
            userDataRepository.getComments(readIndex),
            userDataRepository.getHighlights(readIndex)
        ) { comments, highlights ->
            ReadUserContent(
                readIndex,
                comments.getOrNull() ?: initial.comments,
                highlights.getOrNull() ?: initial.highlights
            )
        }.stateIn(viewModelScope, initial)
    }

}
