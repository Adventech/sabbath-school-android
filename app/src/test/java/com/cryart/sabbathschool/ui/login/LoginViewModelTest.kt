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
import app.cash.turbine.test
import app.ss.auth.AuthRepository
import app.ss.auth.AuthResponse
import app.ss.models.auth.SSUser
import com.cryart.sabbathschool.R
import com.cryart.sabbathschool.core.extensions.coroutines.DispatcherProvider
import com.cryart.sabbathschool.core.model.ViewState
import com.cryart.sabbathschool.core.response.Resource
import com.cryart.sabbathschool.reminder.DailyReminderManager
import com.cryart.sabbathschool.test.coroutines.TestDispatcherProvider
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Test

class LoginViewModelTest {

    private val mockGoogleSignIn: GoogleSignInWrapper = mockk()
    private val mockReminderManager: DailyReminderManager = mockk()
    private val mockAuthRepository: AuthRepository = mockk()

    private val dispatcherProvider: DispatcherProvider = TestDispatcherProvider()

    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        every { mockReminderManager.scheduleReminder() }.returns(Unit)

        viewModel = LoginViewModel(
            authRepository = mockAuthRepository,
            googleSignIn = mockGoogleSignIn,
            reminderManager = mockReminderManager,
            dispatcherProvider = dispatcherProvider
        )
    }

    @Test
    fun `should post Loading then Error when null data is passed to handleGoogleSignInResult`() = runTest {
        viewModel.viewStateFlow.test {
            viewModel.handleGoogleSignInResult(null)

            awaitItem() shouldBeEqualTo null
            awaitItem() shouldBeEqualTo ViewState.Error(messageRes = R.string.ss_login_failed)
        }
    }

    @Test
    fun `should post Loading then Error when handleAnonymousLogin fails`() = runTest {
        coEvery { mockAuthRepository.signIn() }.returns(Resource.error(Throwable("Error")))

        viewModel.viewStateFlow.test {
            viewModel.handleAnonymousLogin()

            awaitItem() shouldBeEqualTo null
            awaitItem() shouldBeEqualTo ViewState.Error(messageRes = R.string.ss_login_failed)
        }
    }

    @Test
    fun `should post Success when Google auth is successful`() = runTest {
        val mockData: Intent = mockk()
        val mockTask: Task<GoogleSignInAccount> = mockk()
        val token = "token"
        val mockAccount: GoogleSignInAccount = mockk()
        val mockUser: SSUser = mockk()

        every { mockAccount.idToken }.returns(token)
        every { mockGoogleSignIn.getSignedInAccountFromIntent(mockData) }
            .returns(mockTask)
        every { mockTask.getResult(ApiException::class.java) }.returns(mockAccount)
        coEvery { mockAuthRepository.signIn(token) }.returns(Resource.success(AuthResponse.Authenticated(mockUser)))

        viewModel.viewStateFlow.test {
            viewModel.handleGoogleSignInResult(mockData)

            awaitItem() shouldBeEqualTo null
            awaitItem() shouldBeEqualTo ViewState.Success(mockUser)
        }
    }

    @Test
    fun `should post Error when Google auth fails`() = runTest {
        val mockData: Intent = mockk()
        val mockTask: Task<GoogleSignInAccount> = mockk()
        val token = "token"
        val mockAccount: GoogleSignInAccount = mockk()

        every { mockAccount.idToken }.returns(token)
        every { mockGoogleSignIn.getSignedInAccountFromIntent(mockData) }
            .returns(mockTask)
        every { mockTask.getResult(ApiException::class.java) }.returns(mockAccount)

        viewModel.viewStateFlow.test {
            viewModel.handleGoogleSignInResult(mockData)

            awaitItem() shouldBeEqualTo null
            awaitItem() shouldBeEqualTo ViewState.Error(messageRes = R.string.ss_login_failed)
        }
    }

    @Test
    fun `should post Success when sign in anonymously is successful`() = runTest {
        val mockUser: SSUser = mockk()

        coEvery { mockAuthRepository.signIn() }.returns(Resource.success(AuthResponse.Authenticated(mockUser)))

        viewModel.viewStateFlow.test {
            viewModel.handleAnonymousLogin()

            awaitItem() shouldBeEqualTo null
            awaitItem() shouldBeEqualTo ViewState.Success(mockUser)

            verify { mockReminderManager.scheduleReminder() }
        }
    }
}
