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

package com.cryart.sabbathschool.readings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ss.lessons.data.model.SSLessonInfo
import app.ss.lessons.data.repository.lessons.LessonsRepository
import app.ss.lessons.data.response.Resource
import com.cryart.sabbathschool.core.extensions.coroutines.SchedulerProvider
import com.cryart.sabbathschool.core.extensions.logger.timber
import com.cryart.sabbathschool.core.misc.SSConstants
import com.cryart.sabbathschool.readings.components.model.AppBarData
import com.cryart.sabbathschool.readings.components.model.ErrorData
import com.cryart.sabbathschool.readings.components.model.ReadingDay
import com.cryart.sabbathschool.readings.components.model.ReadingDaysData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ReadingViewModel @Inject constructor(
    private val lessonsRepository: LessonsRepository,
    private val schedulerProvider: SchedulerProvider
) : ViewModel() {

    private val logger by timber()

    private val _uiState = MutableSharedFlow<UiState>()
    val uiStateFlow: SharedFlow<UiState> get() = _uiState.asSharedFlow()

    private val _appBarData = MutableStateFlow<AppBarData>(AppBarData.Empty)
    val appBarDataFlow: StateFlow<AppBarData> get() = _appBarData.asStateFlow()

    private val _readDays = MutableStateFlow<ReadingDaysData>(ReadingDaysData.Empty)
    val readDaysFlow: StateFlow<ReadingDaysData> get() = _readDays.asStateFlow()

    private val _errorData = MutableStateFlow<ErrorData>(ErrorData.Empty)
    val errorDataFlow: StateFlow<ErrorData> get() = _errorData.asStateFlow()

    fun loadData(lessonIndex: String) = viewModelScope.launch(schedulerProvider.default) {
        val resource = try {
            _uiState.emit(UiState.Loading)
            lessonsRepository.getLessonInfo(lessonIndex)
        } catch (er: Throwable) {
            logger.e(er)
            Resource.error(er)
        }

        val lessonInfo = resource.data
        if (lessonInfo != null) {
            displayLessonInfo(lessonInfo)
        } else {
            _uiState.emit(UiState.Error)
            _errorData.emit(ErrorData.Data(errorRes = R.string.ss_reading_error))
        }
    }

    private fun displayLessonInfo(lessonInfo: SSLessonInfo) = viewModelScope.launch(schedulerProvider.default) {
        val days = lessonInfo.days.map {
            ReadingDay(it.id, it.index, formatDate(it.date), it.title)
        }

        if (days.isEmpty()) {
            _uiState.emit(UiState.Error)
            _errorData.value = ErrorData.Data(errorRes = R.string.ss_reading_empty)
            return@launch
        }

        _uiState.emit(UiState.Success)
        _appBarData.value = AppBarData.Cover(lessonInfo.lesson.cover)

        var index = (_readDays.value as? ReadingDaysData.Days)?.index ?: 0
        if (index == 0) {
            val today = DateTime.now().withTimeAtStartOfDay()
            for ((idx, ssDay) in lessonInfo.days.withIndex()) {
                val startDate = DateTimeFormat.forPattern(SSConstants.SS_DATE_FORMAT)
                    .parseLocalDate(ssDay.date).toDateTimeAtStartOfDay()
                if (startDate.isEqual(today)) {
                    index = idx
                    break
                }
            }
        }

        _readDays.value = ReadingDaysData.Days(days, index)
    }

    private fun formatDate(date: String): String {
        return try {
            DateTimeFormat.forPattern(SSConstants.SS_DATE_FORMAT_OUTPUT_DAY)
                .print(
                    DateTimeFormat.forPattern(SSConstants.SS_DATE_FORMAT)
                        .parseLocalDate(date)
                ).capitalize(Locale.getDefault())
        } catch (ex: IllegalArgumentException) {
            logger.e(ex)
            return ""
        }
    }

    fun onPageSelected(position: Int) {
        val data = _readDays.value
        if (data is ReadingDaysData.Days) {
            val lesson = data.days.getOrNull(position) ?: return
            _appBarData.value = AppBarData.Title(lesson.title, lesson.date)
        }
    }

    fun saveSelectedPage(position: Int) {
        val data = _readDays.value
        if (data is ReadingDaysData.Days && data.days.size > position) {
            _readDays.value = data.copy(index = position)
        }
    }
}
