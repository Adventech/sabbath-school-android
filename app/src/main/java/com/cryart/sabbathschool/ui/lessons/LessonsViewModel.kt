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

package com.cryart.sabbathschool.ui.lessons

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cryart.sabbathschool.data.repository.QuarterliesRepository
import com.cryart.sabbathschool.extensions.arch.SingleLiveEvent
import com.cryart.sabbathschool.misc.SSConstants
import com.cryart.sabbathschool.model.SSQuarterly
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext

class LessonsViewModel @Inject constructor(private val repository: QuarterliesRepository,
                                           private val preferences: SharedPreferences,
                                           @Named("backgroundCoroutineContext")
                                           private val backgroundContext: CoroutineContext) : ViewModel() {

    private val mutableQuarterlyTypes = MutableLiveData<List<String>>()
    val quarterlyTypesLiveData: LiveData<List<String>> get() = mutableQuarterlyTypes

    private val mutableSelectedType = SingleLiveEvent<Pair<String, String?>>()
    val selectedTypeLiveData: LiveData<Pair<String, String?>> get() = mutableSelectedType

    private var lessonTypes: List<SSQuarterly> = emptyList()

    fun setQuarterlyIndex(index: String) {
        viewModelScope.launch(backgroundContext) {
            val resource = repository.getQuarterlies()
            if (resource.isSuccessFul) {
                val quarterlies = resource.data ?: return@launch
                val selected = quarterlies.find { it.index == index } ?: return@launch
                lessonTypes = quarterlies.filter {
                    it.start_date == selected.start_date && it.end_date == selected.end_date
                }
                if (lessonTypes.size > 1) {
                    val names = listOf(selected.quarterly_name) + lessonTypes
                            .filterNot { it.id == selected.id }
                            .map { it.quarterly_name }
                    mutableQuarterlyTypes.postValue(names.filterNotNull())

                    val lastType = preferences.getString(SSConstants.SS_LAST_QUARTERLY_TYPE, null)
                            ?: return@launch
                    if (lastType != names.first()) {
                        quarterlyTypeSelected(lastType)
                    }
                }
            }
        }
    }

    fun quarterlyTypeSelected(type: String) {
        val index = lessonTypes.find { it.quarterly_name == type }?.index ?: return
        mutableSelectedType.postValue(Pair(index, type))
        preferences.edit {
            putString(SSConstants.SS_LAST_QUARTERLY_TYPE, type)
        }
    }
}