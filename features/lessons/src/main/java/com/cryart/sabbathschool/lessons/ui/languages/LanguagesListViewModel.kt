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

package com.cryart.sabbathschool.lessons.ui.languages

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ss.lessons.data.repository.quarterly.QuarterliesRepository
import app.ss.models.Language
import com.cryart.sabbathschool.core.extensions.coroutines.flow.stateIn
import com.cryart.sabbathschool.core.extensions.prefs.SSPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class LanguagesListViewModel @Inject constructor(
    private val repository: QuarterliesRepository,
    private val ssPrefs: SSPrefs
) : ViewModel() {

    val languagesFlow: StateFlow<List<LanguageModel>>
        get() = repository.getLanguages()
            .map { resource ->
                resource.data?.map {
                    LanguageModel(
                        code = it.code,
                        nativeName = getNativeLanguageName(it),
                        name = it.name,
                        selected = it.code == ssPrefs.getLanguageCode()
                    )
                } ?: emptyList()
            }.stateIn(viewModelScope, emptyList())

    @VisibleForTesting
    fun getNativeLanguageName(language: Language): String {
        val loc = Locale(language.code)
        val name = loc.getDisplayLanguage(loc).takeUnless { it == language.code } ?: language.name
        return name.replaceFirstChar { it.uppercase() }
    }
}
