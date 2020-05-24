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

package com.cryart.sabbathschool.ui.languages

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cryart.sabbathschool.data.repository.QuarterliesRepository
import com.cryart.sabbathschool.extensions.arch.asLiveData
import com.cryart.sabbathschool.misc.SSConstants
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext

class LanguagesListViewModel @Inject constructor(private val repository: QuarterliesRepository,
                                                 private val preferences: SharedPreferences,
                                                 @Named("backgroundCoroutineContext")
                                                 private val backgroundContext: CoroutineContext) : ViewModel() {

    private val mutableLanguages = MutableLiveData<List<LanguageModel>>()
    val languagesLiveData: LiveData<List<LanguageModel>> = mutableLanguages.asLiveData()

    init {
        viewModelScope.launch(backgroundContext) {
            val resource = repository.getLanguages()
            if (resource.isSuccessFul) {
                val selectedLanguage = preferences.getString(
                        SSConstants.SS_LAST_LANGUAGE_INDEX,
                        Locale.getDefault().language)!!

                val models = resource.data?.map {
                    LanguageModel(
                            it.code,
                            getNativeLanguageName(it.code),
                            it.name,
                            it.code == selectedLanguage)
                } ?: emptyList()
                mutableLanguages.postValue(models)
            }
        }
    }

    private fun getNativeLanguageName(code: String): String {
        val loc = Locale(code)
        val name = loc.getDisplayLanguage(loc)
        return name.substring(0, 1).toUpperCase(Locale.getDefault()) + name.substring(1)
    }
}