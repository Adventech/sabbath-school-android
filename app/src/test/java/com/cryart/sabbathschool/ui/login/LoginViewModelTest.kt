/*
 * Copyright (c) 2021. Adventech <info@adventech.io>
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
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.cryart.sabbathschool.R
import com.cryart.sabbathschool.core.extensions.coroutines.SchedulerProvider
import com.cryart.sabbathschool.core.model.ViewState
import com.cryart.sabbathschool.observeFuture
import com.cryart.sabbathschool.reminder.DailyReminderManager
import com.facebook.AccessToken
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private val mockFirebaseAuth: FirebaseAuth = mockk()
    private val mockGoogleSignIn: GoogleSignInWrapper = mockk()
    private val mockFacebookLoginManager: FacebookLoginManager = mockk()
    private val mockReminderManager: DailyReminderManager = mockk()

    private val schedulerProvider: SchedulerProvider = SchedulerProvider(
        TestCoroutineDispatcher(), TestCoroutineDispatcher()
    )

    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        every { mockFacebookLoginManager.registerCallback(any(), any()) }.returns(Unit)
        every { mockReminderManager.scheduleReminder() }.returns(Unit)

        viewModel = LoginViewModel(
            mockFirebaseAuth,
            mockGoogleSignIn,
            mockFacebookLoginManager,
            mockReminderManager,
            schedulerProvider
        )
    }

    @Test
    fun `should post Loading then Error when null data is passed to handleGoogleSignInResult`() {
        val states = viewModel.viewStateLiveData.observeFuture()

        viewModel.handleGoogleSignInResult(null)

        states.first() shouldBeEqualTo ViewState.Loading
        states.last() shouldBeEqualTo ViewState.Error(messageRes = R.string.ss_login_failed)
    }

    @Test
    fun `should post Loading then Error when null data is passed to handleAnonymousLogin`() {
        val states = viewModel.viewStateLiveData.observeFuture()

        viewModel.handleGoogleSignInResult(null)

        states.first() shouldBeEqualTo ViewState.Loading
        states.last() shouldBeEqualTo ViewState.Error(messageRes = R.string.ss_login_failed)
    }

    @Test
    fun `should post Success when Google auth is successful`() = runBlockingTest {
        val mockData: Intent = mockk()
        val mockTask: Task<GoogleSignInAccount> = mockk()
        val token = "token"
        val mockAccount: GoogleSignInAccount = mockk()
        val mockAuthCredential: AuthCredential = mockk()
        val mockAuthResult: AuthResult = mockk()
        val mockFirebaseUser: FirebaseUser = mockk()

        every { mockAuthResult.user }.returns(mockFirebaseUser)
        every { mockAccount.idToken }.returns(token)
        every { mockGoogleSignIn.getSignedInAccountFromIntent(mockData) }
            .returns(mockTask)
        every { mockTask.getResult(ApiException::class.java) }.returns(mockAccount)
        every { mockGoogleSignIn.getCredential(token) }.returns(mockAuthCredential)
        every { mockFirebaseAuth.signInWithCredential(mockAuthCredential) }
            .returns(Tasks.forResult(mockAuthResult))

        viewModel.handleGoogleSignInResult(mockData)

        viewModel.viewStateLiveData.value shouldBeEqualTo ViewState.Success(mockFirebaseUser)
    }

    @Test
    fun `should post Error when Google auth is fails`() = runBlockingTest {
        val mockData: Intent = mockk()
        val mockTask: Task<GoogleSignInAccount> = mockk()
        val token = "token"
        val mockAccount: GoogleSignInAccount = mockk()
        val mockAuthCredential: AuthCredential = mockk()
        val mockAuthResult: AuthResult = mockk()

        every { mockAuthResult.user }.returns(null)
        every { mockAccount.idToken }.returns(token)
        every { mockGoogleSignIn.getSignedInAccountFromIntent(mockData) }
            .returns(mockTask)
        every { mockTask.getResult(ApiException::class.java) }.returns(mockAccount)
        every { mockGoogleSignIn.getCredential(token) }.returns(mockAuthCredential)
        every { mockFirebaseAuth.signInWithCredential(mockAuthCredential) }
            .returns(Tasks.forResult(mockAuthResult))

        viewModel.handleGoogleSignInResult(mockData)

        viewModel.viewStateLiveData.value shouldBeEqualTo ViewState.Error(
            messageRes = R.string.ss_login_failed
        )
    }

    @Test
    fun `should post Success when sign in anonymously is successful`() = runBlockingTest {
        val mockAuthResult: AuthResult = mockk()
        val mockFirebaseUser: FirebaseUser = mockk()

        every { mockAuthResult.user }.returns(mockFirebaseUser)
        every { mockFirebaseAuth.signInAnonymously() }
            .returns(Tasks.forResult(mockAuthResult))

        viewModel.handleAnonymousLogin()

        verify { mockReminderManager.scheduleReminder() }

        viewModel.viewStateLiveData.value shouldBeEqualTo ViewState.Success(mockFirebaseUser)
    }

    @Test
    fun `should post Error when sign in anonymously not successful`() = runBlockingTest {
        val mockAuthResult: AuthResult = mockk()

        every { mockAuthResult.user }.returns(null)
        every { mockFirebaseAuth.signInAnonymously() }
            .returns(Tasks.forResult(mockAuthResult))

        viewModel.handleAnonymousLogin()

        viewModel.viewStateLiveData.value shouldBeEqualTo ViewState.Error(
            messageRes = R.string.ss_login_failed
        )
    }

    @Test
    fun `should not interact with firebase auth when facebook sign-in is cancelled`() {
        val callbackSlot: CapturingSlot<FacebookCallback<LoginResult>> = slot()

        viewModel.initFacebookAuth(mockk())

        verify { mockFacebookLoginManager.registerCallback(any(), capture(callbackSlot)) }

        callbackSlot.captured.onCancel()

        verify(inverse = true) { mockFirebaseAuth.signInWithCredential(any()) }
    }

    @Test
    fun `should post Error when facebook sign-in has failed`() {
        val callbackSlot: CapturingSlot<FacebookCallback<LoginResult>> = slot()

        viewModel.initFacebookAuth(mockk())

        verify { mockFacebookLoginManager.registerCallback(any(), capture(callbackSlot)) }

        callbackSlot.captured.onError(FacebookException("Developer Error"))

        viewModel.viewStateLiveData.value shouldBeEqualTo ViewState.Error(
            messageRes = R.string.ss_login_failed
        )
    }

    @Test
    fun `should post Success when facebook sign-in is successful`() {
        val mockLoginResult: LoginResult = mockk()
        val mockAccessToken: AccessToken = mockk()
        val mockCredential: AuthCredential = mockk()
        val mockAuthResult: AuthResult = mockk()
        val mockFirebaseUser: FirebaseUser = mockk()
        val token = "token"

        every { mockAccessToken.token }.returns(token)
        every { mockLoginResult.accessToken }.returns(mockAccessToken)
        every { mockFacebookLoginManager.getCredential(token) }.returns(mockCredential)
        every { mockAuthResult.user }.returns(mockFirebaseUser)
        every { mockFirebaseAuth.signInWithCredential(mockCredential) }
            .returns(Tasks.forResult(mockAuthResult))

        val callbackSlot: CapturingSlot<FacebookCallback<LoginResult>> = slot()

        viewModel.initFacebookAuth(mockk())

        verify { mockFacebookLoginManager.registerCallback(any(), capture(callbackSlot)) }

        callbackSlot.captured.onSuccess(mockLoginResult)

        viewModel.viewStateLiveData.value shouldBeEqualTo ViewState.Success(mockFirebaseUser)
    }
}
