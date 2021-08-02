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
import app.ss.lessons.data.model.SSQuarterly
import app.ss.lessons.data.model.SSQuarterlyInfo
import app.ss.lessons.data.repository.quarterly.QuarterliesRepository
import app.ss.widgets.AppWidgetHelper
import com.cryart.sabbathschool.core.extensions.coroutines.SchedulerProvider
import com.cryart.sabbathschool.core.extensions.prefs.SSPrefs
import com.cryart.sabbathschool.core.misc.SSConstants
import com.cryart.sabbathschool.core.response.Resource
import com.cryart.sabbathschool.lessons.ui.lessons.components.LessonTypeModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LessonsViewModel @Inject constructor(
    private val repository: QuarterliesRepository,
    private val ssPrefs: SSPrefs,
    private val appWidgetHelper: AppWidgetHelper,
    private val schedulerProvider: SchedulerProvider,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _quarterlyInfoData = MutableStateFlow<Resource<SSQuarterlyInfo>>(Resource.loading())
    val quarterlyInfoFlow: StateFlow<Resource<SSQuarterlyInfo>> get() = _quarterlyInfoData.asStateFlow()

    private val _quarterlyTypes = MutableStateFlow<LessonTypeModel?>(null)
    val lessonTypesFlow: StateFlow<LessonTypeModel?> get() = _quarterlyTypes.asStateFlow()

    private val ssQuarterlyInfo: SSQuarterlyInfo? get() = _quarterlyInfoData.value.data
    val quarterlyShareIndex: String get() = ssQuarterlyInfo?.shareIndex() ?: ""
    val quarterlyTitle: String get() = ssQuarterlyInfo?.quarterly?.title ?: ""

    private var lessonTypes: List<SSQuarterly> = emptyList()

    init {
        // cache DisplayOptions for read screen launch
        ssPrefs.getDisplayOptions { }

        val index = savedStateHandle.get<String>(SSConstants.SS_QUARTERLY_INDEX_EXTRA) ?: ssPrefs.getLastQuarterlyIndex()
        if (index?.isNotEmpty() == true) {
            // Grouping is disabled for now
            // setQuarterlyIndex(index)
            loadQuarterlyInfo(index)
        }
    }

    private fun setQuarterlyIndex(index: String) = viewModelScope.launch(schedulerProvider.io) {
        repository.getQuarterlies().collect { resource ->
            val quarterlies = resource.data ?: return@collect
            val selected = quarterlies.find { it.index == index } ?: return@collect

            lessonTypes = quarterlies.filter { it.quarterly_group == selected.quarterly_group }

            if (lessonTypes.size > 1) {
                val names = listOf(selected.quarterly_name) + lessonTypes
                    .filterNot { it.id == selected.id }
                    .map { it.quarterly_name }

                val model = LessonTypeModel(selected.quarterly_name, selected.color_primary, names)
                _quarterlyTypes.emit(model)

                val lastType = ssPrefs.getLastQuarterlyType() ?: return@collect
                if (lastType != names.first()) {
                    quarterlyTypeSelected(lastType)
                }
            }
        }
    }

    fun quarterlyTypeSelected(type: String) {
        val selected = lessonTypes.find { it.quarterly_name == type } ?: return
        ssPrefs.setLastQuarterlyType(type)

        loadQuarterlyInfo(selected.index)

        _quarterlyTypes.value?.let { model ->
            viewModelScope.launch {
                _quarterlyTypes.emit(
                    model.copy(
                        selected = selected.quarterly_name,
                        selectedPrimaryColor = selected.color_primary
                    )
                )
            }
        }
    }

    private fun loadQuarterlyInfo(index: String) = viewModelScope.launch(schedulerProvider.io) {
        val resource = repository.getQuarterlyInfo(index)
        _quarterlyInfoData.emit(resource)

        if (resource.isSuccessFul) {
            ssPrefs.setLastQuarterlyIndex(index)
            appWidgetHelper.refreshAll()
        }
    }
}
