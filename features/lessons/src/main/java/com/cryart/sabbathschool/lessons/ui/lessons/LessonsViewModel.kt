/*
 * Copyright (c) 2020 Adventech <info@adventech.io>
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

package com.cryart.sabbathschool.lessons.ui.lessons

import android.content.Context
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ss.lessons.data.repository.lessons.LessonsRepository
import app.ss.lessons.data.repository.quarterly.QuarterliesRepository
import app.ss.models.LessonPdf
import app.ss.models.PublishingInfo
import app.ss.models.SSQuarterlyInfo
import app.ss.widgets.AppWidgetHelper
import com.cryart.sabbathschool.core.extensions.coroutines.flow.stateIn
import com.cryart.sabbathschool.core.extensions.prefs.SSPrefs
import com.cryart.sabbathschool.core.response.Result
import com.cryart.sabbathschool.core.response.asResult
import com.cryart.sabbathschool.lessons.navigation.lessonIndexArg
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import app.ss.translations.R as L10n

@HiltViewModel
class LessonsViewModel @Inject constructor(
    private val repository: QuarterliesRepository,
    private val lessonsRepository: LessonsRepository,
    private val ssPrefs: SSPrefs,
    private val appWidgetHelper: AppWidgetHelper,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val quarterlyIndex: String?
        get() = savedStateHandle.get<String>(lessonIndexArg) ?: ssPrefs.getLastQuarterlyIndex()

    private val publishingInfo: Flow<Result<PublishingInfo?>> = repository.getPublishingInfo()
        .map { it.data }
        .asResult()

    private val quarterlyInfo: Flow<Result<SSQuarterlyInfo?>> = snapshotFlow { quarterlyIndex }
        .flatMapLatest { index ->
            index?.run {
                repository.getQuarterlyInfo(index)
                    .map { it.data }
            } ?: flowOf(null)
        }
        .onEach { info ->
            info?.run { appWidgetHelper.refreshAll() }
        }
        .asResult()

    private val _selectedPdfs = MutableSharedFlow<Pair<String, List<LessonPdf>>>()
    val selectedPdfsFlow: SharedFlow<Pair<String, List<LessonPdf>>> = _selectedPdfs

    val uiState: StateFlow<LessonsScreenState> = combine(
        publishingInfo,
        quarterlyInfo
    ) { publishingInfo, quarterlyInfo ->

        val publishingInfoState = when (publishingInfo) {
            is Result.Error -> PublishingInfoState.Error
            Result.Loading -> PublishingInfoState.Loading
            is Result.Success -> publishingInfo.data?.let {
                PublishingInfoState.Success(it)
            } ?: PublishingInfoState.Error
        }
        val quarterlyInfoState = when (quarterlyInfo) {
            is Result.Error -> QuarterlyInfoState.Error
            Result.Loading -> QuarterlyInfoState.Loading
            is Result.Success -> quarterlyInfo.data?.let {
                QuarterlyInfoState.Success(it)
            } ?: QuarterlyInfoState.Error
        }

        LessonsScreenState(
            isLoading = quarterlyInfoState == QuarterlyInfoState.Loading,
            isError = quarterlyInfoState == QuarterlyInfoState.Error,
            publishingInfo = publishingInfoState,
            quarterlyInfo = quarterlyInfoState
        )
    }.stateIn(viewModelScope, LessonsScreenState())

    private val ssQuarterlyInfo: SSQuarterlyInfo? get() = (uiState.value.quarterlyInfo as? QuarterlyInfoState.Success)?.quarterlyInfo
    val quarterlyShareIndex: String get() = ssQuarterlyInfo?.shareIndex() ?: ""

    init {
        // cache DisplayOptions for read screen launch
        ssPrefs.getDisplayOptions { }
    }

    fun pdfLessonSelected(lessonIndex: String) = viewModelScope.launch {
        val resource = lessonsRepository.getLessonInfo(lessonIndex)
        if (resource.isSuccessFul) {
            val data = resource.data
            _selectedPdfs.emit(lessonIndex to (data?.pdfs ?: emptyList()))
        }
    }

    fun shareLessonContent(context: Context): String {
        val link = "${context.getString(L10n.string.ss_app_host)}/$quarterlyShareIndex"
        return "${uiState.value.quarterlyTitle}\n$link"
    }
}
