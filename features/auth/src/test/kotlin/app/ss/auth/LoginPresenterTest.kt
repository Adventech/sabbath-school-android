/*
 * Copyright (c) 2024. Adventech <info@adventech.io>
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
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package app.ss.auth

import android.content.Context
import androidx.compose.material3.SnackbarResult
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.ss.auth.test.FakeAuthRepository
import app.ss.design.compose.extensions.content.ContentSpec
import app.ss.models.auth.SSUser
import app.ss.models.config.AppConfig
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.test
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import org.junit.runner.RunWith
import ss.foundation.coroutines.test.TestDispatcherProvider
import ss.libraries.circuit.navigation.CustomTabsIntentScreen
import ss.libraries.circuit.navigation.HomeNavScreen
import ss.libraries.circuit.navigation.LoginScreen
import app.ss.translations.R as L10nR

private const val WEB_CLIENT_ID = "web_id"

@RunWith(AndroidJUnit4::class)
class LoginPresenterTest {

    private val fakeNavigator = FakeNavigator(LoginScreen)
    private val appConfig = AppConfig(version = "", webClientId = WEB_CLIENT_ID)
    private val fakeCredentialManager = FakeCredentialManagerWrapper()
    private val fakeAuthRepository = FakeAuthRepository()

    private val context: Context = ApplicationProvider.getApplicationContext()

    private val underTest = LoginPresenter(
        navigator = fakeNavigator,
        appConfig = appConfig,
        credentialManager = fakeCredentialManager,
        authRepository = fakeAuthRepository,
        dispatcherProvider = TestDispatcherProvider(),
    )

    @Test
    fun `present - default state`() = runTest {
        underTest.test {
            val state = awaitItem()

            (state is State.Default) shouldBeEqualTo true

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - event - SignInAnonymously - dismiss`() = runTest {
        underTest.test {
            var state: State = awaitItem()

            (state as State.Default).eventSink(Event.SignInAnonymously)

            state = awaitItem() as State.ConfirmSignInAnonymously

            state.eventSink(OverlayEvent.Dismiss)

            awaitItem() as State.Default

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - event - SignInAnonymously - confirm`() = runTest {
        fakeAuthRepository.signInDelegate = {
            Result.success(AuthResponse.Authenticated(SSUser.fake()))
        }
        underTest.test {
            var state: State = awaitItem()

            (state as State.Default).eventSink(Event.SignInAnonymously)

            state = awaitItem() as State.ConfirmSignInAnonymously

            state.eventSink(OverlayEvent.Confirm)

            awaitItem() as State.Loading

            fakeNavigator.awaitResetRoot().newRoot shouldBeEqualTo HomeNavScreen

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - event - SignInAnonymously - confirm - auth failure`() = runTest {
        fakeAuthRepository.signInDelegate = {
            Result.success(AuthResponse.Error)
        }
        underTest.test {
            var state: State = awaitItem()

            (state as State.Default).eventSink(Event.SignInAnonymously)

            state = awaitItem() as State.ConfirmSignInAnonymously

            state.eventSink(OverlayEvent.Confirm)

            awaitItem() as State.Loading

            state = awaitItem() as State.Default

            val snackbarState = state.snackbarState
            snackbarState?.message shouldBeEqualTo ContentSpec.Res(L10nR.string.ss_login_failed)

            snackbarState?.onResult?.invoke(SnackbarResult.Dismissed)

            state = awaitItem() as State.Default

            state.snackbarState shouldBeEqualTo null

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - event - SignInWithGoogle`() = runTest {
        val token = "token"
        fakeCredentialManager.getCredentialDelegate = {
            val credentialOptions = it.credentialOptions
            assert(credentialOptions.size == 1)
            val option = credentialOptions.first()
            val serverClientId = (option as GetSignInWithGoogleOption).serverClientId

            if (serverClientId == WEB_CLIENT_ID) {
                GetCredentialResponse(
                    GoogleIdTokenCredential(
                        id = "id",
                        idToken = token,
                        displayName = "",
                        familyName = "",
                        givenName = "",
                        profilePictureUri = null,
                        phoneNumber = null
                    )
                )
            } else {
                throw IllegalStateException("Invalid request")
            }
        }
        fakeAuthRepository.signInWithTokenDelegate = {
            if (it == token) {
                Result.success(AuthResponse.Authenticated(SSUser.fake()))
            } else {
                throw IllegalStateException("Invalid token")
            }
        }

        underTest.test {
            val state = awaitItem()

            (state as State.Default).eventSink(Event.SignInWithGoogle(context))

            awaitItem() as State.Loading

            fakeNavigator.awaitResetRoot().newRoot shouldBeEqualTo HomeNavScreen

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - event - OpenPrivacyPolicy`() = runTest {
        underTest.test {
            val state = awaitItem()

            (state as State.Default).eventSink(Event.OpenPrivacyPolicy(context))

            fakeNavigator.awaitNextScreen() shouldBeEqualTo CustomTabsIntentScreen(context.getString(L10nR.string.ss_privacy_policy_url))

            ensureAllEventsConsumed()
        }
    }
}

private class FakeCredentialManagerWrapper : CredentialManagerWrapper {

    var getCredentialDelegate: (GetCredentialRequest) -> GetCredentialResponse = { throw NotImplementedError() }

    override suspend fun getCredential(context: Context, request: GetCredentialRequest): GetCredentialResponse {
        return getCredentialDelegate(request)
    }
}
