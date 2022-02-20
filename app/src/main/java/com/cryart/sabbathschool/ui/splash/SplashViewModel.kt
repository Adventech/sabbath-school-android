/*
 * Copyright (c) 2020. Adventech <info@adventech.io>
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

package com.cryart.sabbathschool.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ss.auth.AuthRepository
import app.ss.models.auth.SSUser
import com.cryart.sabbathschool.core.extensions.coroutines.DispatcherProvider
import com.cryart.sabbathschool.core.extensions.prefs.SSPrefs
import com.cryart.sabbathschool.reminder.DailyReminderManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val ssPrefs: SSPrefs,
    private val authRepository: AuthRepository,
    private val dailyReminderManager: DailyReminderManager,
    private val dispatcherProvider: DispatcherProvider,
) : ViewModel() {

    private val _launchState: MutableStateFlow<LaunchState> = MutableStateFlow(LaunchState.Loading)
    val launchStateFlow: StateFlow<LaunchState> = _launchState

    fun launch() = viewModelScope.launch(dispatcherProvider.default) {
        val resource = authRepository.getUser()
        val user = resource.data

        if (user != null && ssPrefs.reminderEnabled() && ssPrefs.isReminderScheduled().not()) {
            dailyReminderManager.scheduleReminder()
        }

        updateState(user)
    }

    private suspend fun updateState(user: SSUser?) {
        val state = when {
            user == null -> LaunchState.Login
            ssPrefs.getLastQuarterlyIndex() != null && ssPrefs.isReadingLatestQuarterly() ->
                LaunchState.Lessons(ssPrefs.getLastQuarterlyIndex()!!)
            else -> LaunchState.Quarterlies
        }

        _launchState.emit(state)
    }
}
