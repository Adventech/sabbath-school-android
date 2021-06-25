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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cryart.sabbathschool.core.extensions.arch.SingleLiveEvent
import com.cryart.sabbathschool.core.extensions.arch.asLiveData
import com.cryart.sabbathschool.core.extensions.coroutines.SchedulerProvider
import com.cryart.sabbathschool.core.extensions.prefs.SSPrefs
import app.ss.lessons.data.model.SSQuarterly
import app.ss.lessons.data.repository.quarterly.QuarterliesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LessonsViewModel @Inject constructor(
    private val repository: QuarterliesRepository,
    private val ssPrefs: SSPrefs,
    private val schedulerProvider: SchedulerProvider
) : ViewModel() {

    private val mutableQuarterlyTypes = MutableLiveData<List<String>>()
    val quarterlyTypesLiveData: LiveData<List<String>> = mutableQuarterlyTypes.asLiveData()

    private val mutableSelectedType = SingleLiveEvent<Pair<String, String?>>()
    val selectedTypeLiveData: LiveData<Pair<String, String?>> = mutableSelectedType.asLiveData()

    private var lessonTypes: List<SSQuarterly> = emptyList()

    fun setQuarterlyIndex(index: String) = viewModelScope.launch(schedulerProvider.io) {
        repository.getQuarterlies().collect { resource ->
            val quarterlies = resource.data ?: return@collect
            val selected = quarterlies.find { it.index == index } ?: return@collect

            lessonTypes = quarterlies.filter { it.group == selected.group }

            if (lessonTypes.size > 1) {
                val names = listOf(selected.quarterly_name) + lessonTypes
                    .filterNot { it.id == selected.id }
                    .map { it.quarterly_name }
                mutableQuarterlyTypes.postValue(names)

                val lastType = ssPrefs.getLastQuarterlyType() ?: return@collect
                if (lastType != names.first()) {
                    quarterlyTypeSelected(lastType)
                }
            }
        }
    }

    fun quarterlyTypeSelected(type: String) {
        val index = lessonTypes.find { it.quarterly_name == type }?.index ?: return
        mutableSelectedType.postValue(Pair(index, type))
        ssPrefs.setLastQuarterlyType(type)
    }
}
