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
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cryart.sabbathschool.R
import com.cryart.sabbathschool.core.extensions.arch.SingleLiveEvent
import com.cryart.sabbathschool.core.extensions.arch.asLiveData
import com.cryart.sabbathschool.core.extensions.coroutines.SchedulerProvider
import com.cryart.sabbathschool.core.model.ViewState
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class LoginViewModel @ViewModelInject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val googleSignIn: GoogleSignInWrapper,
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
}
