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

package com.cryart.sabbathschool.ui.quarterlies

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cryart.sabbathschool.data.model.Status
import com.cryart.sabbathschool.data.repository.QuarterliesRepository
import com.cryart.sabbathschool.extensions.arch.SingleLiveEvent
import com.cryart.sabbathschool.extensions.arch.asLiveData
import com.cryart.sabbathschool.misc.SSConstants
import com.cryart.sabbathschool.model.SSQuarterly
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext

class QuarterliesViewModel @Inject constructor(private val repository: QuarterliesRepository,
                                               private val preferences: SharedPreferences,
                                               @Named("backgroundCoroutineContext")
                                               private val backgroundContext: CoroutineContext) : ViewModel() {

    private val mutableViewStatus = SingleLiveEvent<Status>()
    val viewStatusLiveData: LiveData<Status> = mutableViewStatus.asLiveData()

    private val mutableQuarterlies = MutableLiveData<List<SSQuarterly>>()
    val quarterliesLiveData: LiveData<List<SSQuarterly>> = mutableQuarterlies.asLiveData()

    private val mutableShowLanguagePrompt = SingleLiveEvent<Any>()
    val showLanguagePromptLiveData: LiveData<Any> = mutableShowLanguagePrompt.asLiveData()

    private val mutableLastQuarterlyIndex = SingleLiveEvent<String>()
    val lastQuarterlyIndexLiveData: LiveData<String> = mutableLastQuarterlyIndex.asLiveData()

    private var selectedLanguage: String = ""

    init {
        preferences.getString(SSConstants.SS_LAST_QUARTERLY_INDEX, null)?.let {
            mutableLastQuarterlyIndex.postValue(it)
        }

        selectedLanguage = preferences.getString(
                SSConstants.SS_LAST_LANGUAGE_INDEX,
                Locale.getDefault().language)!!

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
        viewModelScope.launch(backgroundContext) {
            mutableViewStatus.postValue(Status.LOADING)
            val resource = repository.getQuarterlies(code)
            if (resource.isSuccessFul) {
                val quarterlies = resource.data ?: emptyList()
                val filtered = quarterlies
                        .filter { it.group != null }
                        .distinctBy { it.group } + quarterlies
                        .filter { it.group == null }

                mutableQuarterlies.postValue(filtered)

                val languagePromptSeen = preferences.getBoolean(
                        SSConstants.SS_LANGUAGE_FILTER_PROMPT_SEEN, false)
                if (!languagePromptSeen) {
                    withContext(Dispatchers.Main) {
                        mutableShowLanguagePrompt.call()
                    }
                }
            }
            mutableViewStatus.postValue(resource.status)
        }
    }

    fun languageSelected(languageCode: String) {
        preferences.edit {
            putString(SSConstants.SS_LAST_LANGUAGE_INDEX, languageCode)
        }
        updateQuarterlies(languageCode)
    }

    fun languagesPromptSeen() {
        preferences.edit {
            putBoolean(SSConstants.SS_LANGUAGE_FILTER_PROMPT_SEEN, true)
        }
    }
}