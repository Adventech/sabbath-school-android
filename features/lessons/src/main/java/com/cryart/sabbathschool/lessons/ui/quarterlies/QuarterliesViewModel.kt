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

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ss.auth.AuthRepository
import app.ss.lessons.data.repository.quarterly.QuarterliesRepository
import app.ss.models.QuarterlyGroup
import app.ss.models.SSQuarterly
import com.cryart.sabbathschool.core.response.Resource
import com.cryart.sabbathschool.core.response.Result
import com.cryart.sabbathschool.core.response.asResult
import com.cryart.sabbathschool.lessons.ui.quarterlies.model.GroupedQuarterlies
import com.cryart.sabbathschool.lessons.ui.quarterlies.model.QuarterliesGroupModel
import com.cryart.sabbathschool.lessons.ui.quarterlies.model.QuarterlySpec
import com.cryart.sabbathschool.lessons.ui.quarterlies.model.placeHolderQuarterlies
import com.cryart.sabbathschool.lessons.ui.quarterlies.model.spec
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ss.foundation.coroutines.flow.stateIn
import ss.misc.SSConstants
import ss.prefs.api.SSPrefs
import javax.inject.Inject

@HiltViewModel
class QuarterliesViewModel @Inject constructor(
    private val repository: QuarterliesRepository,
    private val ssPrefs: SSPrefs,
    private val savedStateHandle: SavedStateHandle,
    authRepository: AuthRepository
) : ViewModel() {

    private val quarterlyGroup: QuarterlyGroup?
        get() = savedStateHandle[SSConstants.SS_QUARTERLY_GROUP]

    val groupTitle get() = quarterlyGroup?.name

    private val photo: Flow<Result<String?>> = authRepository.getUserFlow()
        .map { it?.photo }
        .asResult()

    private val quarterlies: Flow<Result<GroupedQuarterlies>> = ssPrefs.getLanguageCodeFlow()
        .flatMapLatest { language -> repository.getQuarterlies(language, quarterlyGroup) }
        .map(this::groupQuarterlies)
        .asResult()

    val uiState = combine(quarterlies, photo) { quarterliesResult, photoResult ->
        val type = when (quarterliesResult) {
            is Result.Error -> GroupedQuarterlies.Empty
            Result.Loading -> GroupedQuarterlies.TypeList(placeHolderQuarterlies())
            is Result.Success -> quarterliesResult.data
        }
        val photoUrl = when (photoResult) {
            is Result.Error,
            Result.Loading -> null
            is Result.Success -> photoResult.data
        }

        QuarterliesUiState(
            isLoading = quarterliesResult is Result.Loading,
            isError = quarterliesResult is Result.Error,
            photoUrl = photoUrl,
            type = type
        )
    }.stateIn(viewModelScope, QuarterliesUiState())

    private val _appReBranding = MutableSharedFlow<Boolean>()
    val appReBrandingFlow: SharedFlow<Boolean> get() = _appReBranding.asSharedFlow()

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
                val specs = grouped[grouped.firstKey()]?.map { it.spec() }
                GroupedQuarterlies.TypeList(specs ?: emptyList())
            }
            grouped.keys.size > 1 -> {
                val filtered = grouped.filterKeys { it != null } as Map<QuarterlyGroup, List<SSQuarterly>>
                if (filtered.keys.size > 1) {
                    val groups = filtered.map { map ->
                        QuarterliesGroupModel(
                            group = map.key.spec(),
                            quarterlies = map.value.map { it.spec(QuarterlySpec.Type.LARGE) }
                        )
                    }
                    GroupedQuarterlies.TypeGroup(groups)
                } else {
                    val specs = filtered[filtered.keys.first()]?.map { it.spec() }
                    GroupedQuarterlies.TypeList(specs ?: emptyList())
                }
            }
            else -> GroupedQuarterlies.Empty
        }

        handleBrandingPrompt()

        return groupType
    }

    fun languageSelected(languageCode: String) {
        ssPrefs.setLanguageCode(languageCode)
        ssPrefs.setLastQuarterlyIndex(null)

        val appLocale = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
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
