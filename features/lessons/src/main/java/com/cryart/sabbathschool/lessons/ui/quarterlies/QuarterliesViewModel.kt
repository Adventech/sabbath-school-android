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

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ss.lessons.data.model.QuarterlyGroup
import app.ss.lessons.data.model.SSQuarterly
import app.ss.lessons.data.repository.quarterly.QuarterliesRepository
import com.cryart.sabbathschool.core.extensions.arch.SingleLiveEvent
import com.cryart.sabbathschool.core.extensions.arch.asLiveData
import com.cryart.sabbathschool.core.extensions.coroutines.SchedulerProvider
import com.cryart.sabbathschool.core.extensions.coroutines.flow.stateIn
import com.cryart.sabbathschool.core.extensions.prefs.SSPrefs
import com.cryart.sabbathschool.core.misc.SSConstants
import com.cryart.sabbathschool.core.model.ViewState
import com.cryart.sabbathschool.core.response.Resource
import com.cryart.sabbathschool.lessons.ui.quarterlies.components.GroupedQuarterlies
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class QuarterliesViewModel @Inject constructor(
    private val repository: QuarterliesRepository,
    private val ssPrefs: SSPrefs,
    private val firebaseAuth: FirebaseAuth,
    private val schedulerProvider: SchedulerProvider,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val mutableViewState = MutableLiveData<ViewState>()
    val viewStateLiveData: LiveData<ViewState> = mutableViewState.asLiveData()

    val photoUrlFlow: SharedFlow<Uri?>
        get() = flowOf(firebaseAuth.currentUser?.photoUrl)
            .stateIn(viewModelScope, null)

    private val mutableShowLanguagePrompt = SingleLiveEvent<Any>()
    val showLanguagePromptLiveData: LiveData<Any> = mutableShowLanguagePrompt.asLiveData()

    private val mutableLastQuarterlyIndex = SingleLiveEvent<String>()
    val lastQuarterlyIndexLiveData: LiveData<String> = mutableLastQuarterlyIndex.asLiveData()

    private val _appReBranding = MutableSharedFlow<Boolean>()
    val appReBrandingFlow: SharedFlow<Boolean> get() = _appReBranding.asSharedFlow()

    val quarterliesFlow: SharedFlow<Resource<GroupedQuarterlies>>
        get() = ssPrefs.getLanguageCodeFlow()
            .map(repository::getQuarterlies)
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
                    GroupedQuarterlies.TypeMap(filtered)
                } else {
                    GroupedQuarterlies.TypeList(filtered[filtered.keys.first()] ?: emptyList())
                }
            }
            else -> GroupedQuarterlies.Empty
        }

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

        val selectedLanguage = ssPrefs.getLanguageCode()
        updateQuarterlies(selectedLanguage)
    }

    private fun updateQuarterlies(code: String) {
        if (code.isEmpty()) {
            return
        }
        viewModelScope.launch(schedulerProvider.io) {
            mutableViewState.postValue(ViewState.Loading)
            val resource = repository.getQuarterlies(code)
            if (resource.isSuccessFul) {
                val quarterlies = resource.data ?: emptyList()

                mutableViewState.postValue(ViewState.Success(quarterlies))

                if (!ssPrefs.isLanguagePromptSeen()) {
                    withContext(schedulerProvider.main) {
                        mutableShowLanguagePrompt.call()
                    }
                } else {
                    handleBrandingPrompt()
                }
            } else {
                mutableViewState.postValue(ViewState.Error())
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

    private suspend fun handleBrandingPrompt() {
        if (!ssPrefs.isAppReBrandingPromptShown()) {
            ssPrefs.setAppReBrandingShown()
            _appReBranding.emit(true)
        }
    }
}
