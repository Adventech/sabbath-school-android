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
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ss.lessons.data.model.QuarterlyGroup
import app.ss.lessons.data.model.SSQuarterly
import app.ss.lessons.data.repository.quarterly.QuarterliesRepository
import com.cryart.sabbathschool.core.extensions.arch.SingleLiveEvent
import com.cryart.sabbathschool.core.extensions.arch.asLiveData
import com.cryart.sabbathschool.core.extensions.coroutines.flow.stateIn
import com.cryart.sabbathschool.core.extensions.prefs.SSPrefs
import com.cryart.sabbathschool.core.misc.SSConstants
import com.cryart.sabbathschool.core.response.Resource
import com.cryart.sabbathschool.lessons.ui.quarterlies.components.GroupedQuarterlies
import com.cryart.sabbathschool.lessons.ui.quarterlies.components.QuarterliesAppbarData
import com.cryart.sabbathschool.lessons.ui.quarterlies.components.QuarterliesGroup
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuarterliesViewModel @Inject constructor(
    private val repository: QuarterliesRepository,
    private val ssPrefs: SSPrefs,
    private val firebaseAuth: FirebaseAuth,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val quarterlyGroup: QuarterlyGroup?
        get() = savedStateHandle.get(SSConstants.SS_QUARTERLY_GROUP)

    val photoUrlFlow: SharedFlow<QuarterliesAppbarData>
        get() = flowOf(QuarterliesAppbarData.Photo(firebaseAuth.currentUser?.photoUrl))
            .stateIn(viewModelScope, QuarterliesAppbarData.Empty)

    val groupTitleFlow: SharedFlow<QuarterliesAppbarData>
        get() = flowOf(
            QuarterliesAppbarData.Title(quarterlyGroup?.name)
        ).stateIn(viewModelScope, QuarterliesAppbarData.Empty)

    private val mutableLastQuarterlyIndex = SingleLiveEvent<String>()
    val lastQuarterlyIndexLiveData: LiveData<String> get() = mutableLastQuarterlyIndex.asLiveData()

    private val _appReBranding = MutableSharedFlow<Boolean>()
    val appReBrandingFlow: SharedFlow<Boolean> get() = _appReBranding.asSharedFlow()

    val quarterliesFlow: StateFlow<Resource<GroupedQuarterlies>>
        get() = ssPrefs.getLanguageCodeFlow()
            .map { language -> repository.getQuarterlies(language, quarterlyGroup) }
            .map(this::groupQuarterlies)
            .stateIn(viewModelScope, Resource.loading())

    @Suppress("UNCHECKED_CAST")
    private fun groupQuarterlies(resource: Resource<List<SSQuarterly>>): Resource<GroupedQuarterlies> {
        val data = resource.data ?: run {
            return resource.error?.let { error -> Resource.error(error) } ?: Resource.loading()
        }
        data.firstOrNull()?.let {
            ssPrefs.setThemeColor(it.color_primary, it.color_primary_dark)
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

        return Resource.success(groupType)
    }

    fun viewCreated() {
        if (savedStateHandle.get<Boolean>(SSConstants.SS_QUARTERLY_SCREEN_LAUNCH_EXTRA) == true) {
            ssPrefs.getLastQuarterlyIndex()?.let {
                if (mutableLastQuarterlyIndex.value.isNullOrEmpty() && ssPrefs.isAppReBrandingPromptShown()) {
                    mutableLastQuarterlyIndex.postValue(it)
                }
            }
        }
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
