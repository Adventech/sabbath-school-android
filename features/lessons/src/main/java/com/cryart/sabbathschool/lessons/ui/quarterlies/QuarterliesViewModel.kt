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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ss.auth.AuthRepository
import app.ss.lessons.data.repository.quarterly.QuarterliesRepository
import app.ss.models.QuarterlyGroup
import app.ss.models.SSQuarterly
import com.cryart.sabbathschool.core.extensions.coroutines.DispatcherProvider
import com.cryart.sabbathschool.core.extensions.coroutines.flow.stateIn
import com.cryart.sabbathschool.core.extensions.prefs.SSPrefs
import com.cryart.sabbathschool.core.misc.SSConstants
import com.cryart.sabbathschool.core.response.Resource
import com.cryart.sabbathschool.lessons.ui.quarterlies.model.GroupedQuarterlies
import com.cryart.sabbathschool.lessons.ui.quarterlies.model.QuarterliesGroup
import com.cryart.sabbathschool.lessons.ui.quarterlies.model.placeHolderQuarterlies
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuarterliesViewModel @Inject constructor(
    private val repository: QuarterliesRepository,
    private val ssPrefs: SSPrefs,
    private val authRepository: AuthRepository,
    private val savedStateHandle: SavedStateHandle,
    dispatcherProvider: DispatcherProvider
) : ViewModel() {

    private val quarterlyGroup: QuarterlyGroup?
        get() = savedStateHandle[SSConstants.SS_QUARTERLY_GROUP]

    private val _photoUrl = MutableStateFlow<String?>(null)
    val photoUrlFlow: StateFlow<String?> = _photoUrl.asStateFlow()

    val groupTitle get() = quarterlyGroup?.name

    private val _appReBranding = MutableSharedFlow<Boolean>()
    val appReBrandingFlow: SharedFlow<Boolean> get() = _appReBranding.asSharedFlow()

    val quarterliesFlow: StateFlow<GroupedQuarterlies> = ssPrefs.getLanguageCodeFlow()
        .flatMapLatest { language -> repository.getQuarterlies(language, quarterlyGroup) }
        .map(this::groupQuarterlies)
        .stateIn(viewModelScope, GroupedQuarterlies.TypeList(placeHolderQuarterlies()))

    init {
        viewModelScope.launch(dispatcherProvider.io) {
            val photoUrl = authRepository.getUser().data?.photo
            _photoUrl.emit(photoUrl)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun groupQuarterlies(resource: Resource<List<SSQuarterly>>): GroupedQuarterlies {
        val data = resource.data ?: run {
            return GroupedQuarterlies.Empty
        }
        val grouped = data
            .groupBy { it.quarterly_group }
            .toSortedMap(compareBy { it?.order })

        val groupType = when {
            grouped.keys.size == 1 -> {
                GroupedQuarterlies.TypeList(grouped[grouped.firstKey()] ?: emptyList())
            }
            grouped.keys.size > 1 -> {
                val filtered = grouped.filterKeys { it != null } as Map<QuarterlyGroup, List<SSQuarterly>>
                if (filtered.keys.size > 1) {
                    val groups = filtered.map { map ->
                        QuarterliesGroup(map.key, map.value)
                    }
                    GroupedQuarterlies.TypeGroup(groups)
                } else {
                    GroupedQuarterlies.TypeList(filtered[filtered.keys.first()] ?: emptyList())
                }
            }
            else -> GroupedQuarterlies.Empty
        }

        handleBrandingPrompt()

        return groupType
    }

    fun languageSelected(languageCode: String) {
        ssPrefs.setLanguageCode(languageCode)
    }

    private fun handleBrandingPrompt() {
        if (ssPrefs.isAppReBrandingPromptShown().not()) {
            ssPrefs.setAppReBrandingShown()
            viewModelScope.launch {
                _appReBranding.emit(true)
            }
        }
    }
}
