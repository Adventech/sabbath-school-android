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

package com.cryart.sabbathschool.lessons.ui.quarterlies

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cryart.sabbathschool.core.extensions.arch.SingleLiveEvent
import com.cryart.sabbathschool.core.extensions.arch.asLiveData
import com.cryart.sabbathschool.core.extensions.coroutines.SchedulerProvider
import com.cryart.sabbathschool.core.extensions.prefs.SSPrefs
import com.cryart.sabbathschool.core.model.ViewState
import app.ss.lessons.data.repository.quarterly.QuarterliesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class QuarterliesViewModel @Inject constructor(
    private val repository: QuarterliesRepository,
    private val ssPrefs: SSPrefs,
    private val schedulerProvider: SchedulerProvider
) : ViewModel() {

    private val mutableViewState = MutableLiveData<ViewState>()
    val viewStateLiveData: LiveData<ViewState> = mutableViewState.asLiveData()

    private val mutableShowLanguagePrompt = SingleLiveEvent<Any>()
    val showLanguagePromptLiveData: LiveData<Any> = mutableShowLanguagePrompt.asLiveData()

    private val mutableLastQuarterlyIndex = SingleLiveEvent<String>()
    val lastQuarterlyIndexLiveData: LiveData<String> = mutableLastQuarterlyIndex.asLiveData()

    private var selectedLanguage: String = ""

    fun viewCreated() {
        ssPrefs.getLastQuarterlyIndex()?.let {
            if (mutableLastQuarterlyIndex.value.isNullOrEmpty()) {
                mutableLastQuarterlyIndex.postValue(it)
            }
        }

        selectedLanguage = ssPrefs.getLanguageCode()

        if (selectedLanguage == "iw") {
            selectedLanguage = "he"
        }
        if (selectedLanguage == "fil") {
            selectedLanguage = "tl"
        }

        updateQuarterlies(selectedLanguage)
    }

    private fun updateQuarterlies(code: String) {
        if (code.isEmpty()) {
            return
        }
        viewModelScope.launch(schedulerProvider.io) {
            mutableViewState.postValue(ViewState.Loading)
            repository.getQuarterlies(code).collect { resource ->
                if (resource.isSuccessFul) {
                    val quarterlies = resource.data ?: emptyList()
                    val filtered = quarterlies
                        .filter { it.group != null }
                        .distinctBy { it.group } + quarterlies
                        .filter { it.group == null }

                    mutableViewState.postValue(ViewState.Success(filtered))

                    val languagePromptSeen = ssPrefs.isLanguagePromptSeen()
                    if (!languagePromptSeen) {
                        withContext(schedulerProvider.main) {
                            mutableShowLanguagePrompt.call()
                        }
                    }
                } else {
                    mutableViewState.postValue(ViewState.Error())
                }
            }
        }
    }

    fun languageSelected(languageCode: String) {
        ssPrefs.setLanguageCode(languageCode)
        updateQuarterlies(languageCode)
    }

    fun languagesPromptSeen() {
        ssPrefs.setLanguagePromptSeen()
    }
}
