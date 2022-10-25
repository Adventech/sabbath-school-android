/*
 * Copyright (c) 2022. Adventech <info@adventech.io>
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

package app.ss.languages

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ss.languages.state.LanguageModel
import app.ss.languages.state.LanguagesState
import app.ss.languages.state.ListState
import app.ss.lessons.data.repository.quarterly.QuarterliesRepository
import com.cryart.sabbathschool.core.extensions.coroutines.flow.stateIn
import com.cryart.sabbathschool.core.extensions.prefs.SSPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class LanguagesViewModel @Inject constructor(
    private val repository: QuarterliesRepository,
    private val ssPrefs: SSPrefs
) : ViewModel() {

    private val searchQuery = MutableStateFlow<String?>(null)
    private val queryFlow: SharedFlow<String?> = searchQuery

    internal val uiState: StateFlow<LanguagesState> = queryFlow
        .debounce(250L)
        .flatMapMerge { repository.getLanguages(it) }
        .map { resource ->
            val list = resource.data?.map {
                LanguageModel(
                    code = it.code,
                    nativeName = it.nativeName,
                    name = it.name,
                    selected = it.code == ssPrefs.getLanguageCode()
                )
            } ?: emptyList()
            LanguagesState(
                isLoading = false,
                query = searchQuery.value,
                listState = ListState(list)
            )
        }.stateIn(viewModelScope, LanguagesState())

    internal fun searchFor(query: String) {
        viewModelScope.launch { searchQuery.emit(query.trim()) }
    }

    internal fun modelSelected(model: LanguageModel) {
        ssPrefs.setLanguageCode(model.code)
        ssPrefs.setLastQuarterlyIndex(null)

        val appLocale = LocaleListCompat.forLanguageTags(model.code)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }
}
