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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ss.lessons.data.model.LessonPdf
import app.ss.models.SSLesson
import app.ss.models.SSQuarterly
import app.ss.models.SSQuarterlyInfo
import app.ss.lessons.data.repository.lessons.LessonsRepository
import app.ss.lessons.data.repository.quarterly.QuarterliesRepository
import app.ss.widgets.AppWidgetHelper
import com.cryart.sabbathschool.core.extensions.coroutines.flow.stateIn
import com.cryart.sabbathschool.core.extensions.prefs.SSPrefs
import com.cryart.sabbathschool.core.misc.DateHelper
import com.cryart.sabbathschool.core.misc.SSConstants
import com.cryart.sabbathschool.core.response.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.joda.time.Interval
import javax.inject.Inject

@HiltViewModel
class LessonsViewModel @Inject constructor(
    private val repository: QuarterliesRepository,
    private val lessonsRepository: LessonsRepository,
    private val ssPrefs: SSPrefs,
    private val appWidgetHelper: AppWidgetHelper,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val quarterlyIndex: String?
        get() = savedStateHandle.get<String>(
            SSConstants.SS_QUARTERLY_INDEX_EXTRA
        ) ?: ssPrefs.getLastQuarterlyIndex()

    val quarterlyInfoFlow: StateFlow<Resource<SSQuarterlyInfo>> = flowOf(quarterlyIndex)
        .map { index ->
            val resource = index?.let {
                repository.getQuarterlyInfo(index)
            } ?: Resource.error(Throwable())

            if (resource.isSuccessFul) {
                appWidgetHelper.refreshAll()
                ssPrefs.setLastQuarterlyIndex(index!!)
                resource.data?.let {
                    ssPrefs.setThemeColor(
                        it.quarterly.color_primary,
                        it.quarterly.color_primary_dark
                    )
                    ssPrefs.setReadingLatestQuarterly(it.quarterly.isLatest)
                }
            }
            resource
        }
        .stateIn(viewModelScope, Resource.loading())

    private val ssQuarterlyInfo: SSQuarterlyInfo? get() = quarterlyInfoFlow.value.data
    val quarterlyShareIndex: String get() = ssQuarterlyInfo?.shareIndex() ?: ""
    val quarterlyTitle: String get() = ssQuarterlyInfo?.quarterly?.title ?: ""

    private val _selectedPdfs = MutableSharedFlow<Pair<String, List<LessonPdf>>>()
    val selectedPdfsFlow: SharedFlow<Pair<String, List<LessonPdf>>> = _selectedPdfs

    init {
        // cache DisplayOptions for read screen launch
        ssPrefs.getDisplayOptions { }
    }

    fun pdfLessonSelected(lesson: SSLesson) = viewModelScope.launch {
        val resource = lessonsRepository.getLessonInfo(lesson.index)
        if (resource.isSuccessFul) {
            val data = resource.data
            _selectedPdfs.emit(lesson.index to (data?.pdfs ?: emptyList()))
        }
    }

    private val SSQuarterly.isLatest: Boolean
        get() {
            val today = DateTime.now().withTimeAtStartOfDay()

            val startDate = DateHelper.parseDate(start_date)
            val endDate = DateHelper.parseDate(end_date)

            return Interval(startDate, endDate?.plusDays(1)).contains(today)
        }
}
