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
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cryart.sabbathschool.R
import com.cryart.sabbathschool.core.extensions.arch.SingleLiveEvent
import com.cryart.sabbathschool.core.extensions.arch.asLiveData
import com.cryart.sabbathschool.core.extensions.coroutines.SchedulerProvider
import com.cryart.sabbathschool.core.model.ViewState
import com.cryart.sabbathschool.reminder.DailyReminderManager
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val googleSignIn: GoogleSignInWrapper,
    private val facebookLoginManager: FacebookLoginManager,
    private val reminderManager: DailyReminderManager,
    private val schedulerProvider: SchedulerProvider
) : ViewModel() {

    private val mutableViewState = SingleLiveEvent<ViewState>()
    val viewStateLiveData: LiveData<ViewState> = mutableViewState.asLiveData()

    fun handleGoogleSignInResult(data: Intent?) {
        mutableViewState.postValue(ViewState.Loading)

        try {
            val task = googleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)

            val token = account?.idToken ?: return
            val credential = googleSignIn.getCredential(token)
            viewModelScope.launch(schedulerProvider.io) {
                val result = firebaseAuth.signInWithCredential(credential).await()
                handleAuthResult(result)
            }
        } catch (e: Exception) {
            Timber.e(e)
            mutableViewState.postValue(ViewState.Error(messageRes = R.string.ss_login_failed))
        }
    }

    private fun handleAuthResult(result: AuthResult) {
        val state = if (result.user != null) {
            reminderManager.scheduleReminder()
            ViewState.Success(result.user)
        } else {
            ViewState.Error(messageRes = R.string.ss_login_failed)
        }
        mutableViewState.postValue(state)
    }

    fun handleAnonymousLogin() {
        mutableViewState.postValue(ViewState.Loading)

        try {
            viewModelScope.launch(schedulerProvider.io) {
                val result = firebaseAuth.signInAnonymously().await()
                handleAuthResult(result)
            }
        } catch (e: Exception) {
            Timber.e(e)
            mutableViewState.postValue(ViewState.Error(messageRes = R.string.ss_login_failed))
        }
    }

    fun initFacebookAuth(manager: CallbackManager) {
        facebookLoginManager.registerCallback(
            manager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    handleFacebookAccessToken(result.accessToken)
                }

                override fun onCancel() {
                    // No-op
                }

                override fun onError(error: FacebookException) {
                    Timber.e(error)
                    mutableViewState.postValue(
                        ViewState.Error(messageRes = R.string.ss_login_failed)
                    )
                }
            }
        )
    }

    private fun handleFacebookAccessToken(accessToken: AccessToken) {
        mutableViewState.postValue(ViewState.Loading)
        try {
            viewModelScope.launch(schedulerProvider.io) {
                val credential = facebookLoginManager.getCredential(accessToken.token)
                val result = firebaseAuth.signInWithCredential(credential).await()
                handleAuthResult(result)
            }
        } catch (e: Exception) {
            Timber.e(e)
            mutableViewState.postValue(ViewState.Error(messageRes = R.string.ss_login_failed))
        }
    }
}
