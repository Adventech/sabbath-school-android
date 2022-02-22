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

package com.cryart.sabbathschool.ui.login

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ss.auth.AuthRepository
import app.ss.auth.AuthResponse
import com.cryart.sabbathschool.R
import com.cryart.sabbathschool.core.extensions.coroutines.DispatcherProvider
import com.cryart.sabbathschool.core.model.ViewState
import com.cryart.sabbathschool.core.response.Resource
import com.cryart.sabbathschool.reminder.DailyReminderManager
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val googleSignIn: GoogleSignInWrapper,
    private val reminderManager: DailyReminderManager,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {

    private val _viewState: MutableStateFlow<ViewState?> = MutableStateFlow(null)
    val viewStateFlow: StateFlow<ViewState?> = _viewState

    fun handleGoogleSignInResult(data: Intent?) = viewModelScope.launch(dispatcherProvider.default) {
        _viewState.emit(ViewState.Loading)

        try {
            val task = googleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)

            val token = account?.idToken ?: return@launch
            val response = authRepository.signIn(token)
            handleAuthResult(response)
        } catch (e: Exception) {
            Timber.e(e)
            _viewState.emit(ViewState.Error(messageRes = R.string.ss_login_failed))
        }
    }

    fun handleAnonymousLogin() = viewModelScope.launch(dispatcherProvider.default) {
        _viewState.emit(ViewState.Loading)
        val response = authRepository.signIn()
        handleAuthResult(response)
    }

    private suspend fun handleAuthResult(response: Resource<AuthResponse>) {
        val state = when (response.data) {
            is AuthResponse.Authenticated -> {
                reminderManager.scheduleReminder()
                ViewState.Success((response.data as AuthResponse.Authenticated).user)
            }
            else -> ViewState.Error(messageRes = R.string.ss_login_failed)
        }

        _viewState.emit(state)
    }
}
