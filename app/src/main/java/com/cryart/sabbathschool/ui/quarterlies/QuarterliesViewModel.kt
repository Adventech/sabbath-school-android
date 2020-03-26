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
import com.cryart.sabbathschool.data.model.Status
import com.cryart.sabbathschool.data.repository.QuarterliesRepository
import com.cryart.sabbathschool.extensions.arch.SingleLiveEvent
import com.cryart.sabbathschool.misc.SSConstants
import com.cryart.sabbathschool.model.SSQuarterly
import com.cryart.sabbathschool.model.SSQuarterlyLanguage
import com.cryart.sabbathschool.viewmodel.ScopedViewModel
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

class QuarterliesViewModel @Inject constructor(private val repository: QuarterliesRepository,
                                               private val preferences: SharedPreferences) : ScopedViewModel() {

    private val mutableViewStatus = SingleLiveEvent<Status>()
    val viewStatusLiveData: LiveData<Status> get() = mutableViewStatus

    private val mutableQuarterlies = MutableLiveData<List<SSQuarterly>>()
    val quarterliesLiveData: LiveData<List<SSQuarterly>> get() = mutableQuarterlies

    private var selectedLanguage: String = ""

    init {
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
        launch {
            mutableViewStatus.postValue(Status.LOADING)
            val resource = repository.getQuarterlies(code)
            if (resource.isSuccessFul) {
                mutableQuarterlies.postValue(resource.data)
            }
            mutableViewStatus.postValue(resource.status)
        }
    }

    fun languageSelected(language: SSQuarterlyLanguage) {
        preferences.edit {
            putString(SSConstants.SS_LAST_LANGUAGE_INDEX, language.code)
        }
        updateQuarterlies(language.code)
    }
}