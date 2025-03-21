/*
 * Copyright (c) 2025. Adventech <info@adventech.io>
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import app.ss.design.compose.extensions.content.ContentSpec
import app.ss.design.compose.extensions.snackbar.SsSnackbarState
import app.ss.models.config.AppConfig
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ss.foundation.coroutines.DispatcherProvider
import ss.libraries.circuit.navigation.CustomTabsIntentScreen
import ss.libraries.circuit.navigation.HomeNavScreen
import ss.libraries.circuit.navigation.LoginScreen
import timber.log.Timber
import app.ss.translations.R as L10nR

class LoginPresenter @AssistedInject constructor(
    @Assisted private val navigator: Navigator,
    private val appConfig: AppConfig,
    private val credentialManager: CredentialManagerWrapper,
    private val authRepository: AuthRepository,
    private val dispatcherProvider: DispatcherProvider,
) : Presenter<State> {

    @CircuitInject(LoginScreen::class, SingletonComponent::class)
    @AssistedFactory
    interface Factory {
        fun create(navigator: Navigator): LoginPresenter
    }

    private val getCredentialRequest: GetCredentialRequest by lazy {
        GetCredentialRequest.Builder()
            .addCredentialOption(GetSignInWithGoogleOption.Builder(appConfig.webClientId).build())
            .build()
    }


    @Composable
    override fun present(): State {
        val scope = rememberCoroutineScope()
        var isLoading by rememberRetained { mutableStateOf(false) }
        var showConfirmAnonymousAuth by rememberRetained { mutableStateOf(false) }
        var snackbarState by rememberRetained { mutableStateOf<SsSnackbarState?>(null) }

        val onAuthError = {
            isLoading = false
            snackbarState = SsSnackbarState(message = ContentSpec.Res(L10nR.string.ss_login_failed)) {
                snackbarState = null
            }
        }
        val eventSink: (Event) -> Unit = { event ->
            when (event) {
                is Event.SignInAnonymously -> showConfirmAnonymousAuth = true
                is Event.SignInWithGoogle -> {
                    isLoading = true
                    scope.launch {
                        val isAuthenticated = authWithGoogle(event.context).getOrElse { false }
                        if (isAuthenticated) {
                            navigator.resetRoot(HomeNavScreen)
                        } else {
                            onAuthError()
                        }
                    }
                }

                is Event.OpenPrivacyPolicy ->
                    navigator.goTo(CustomTabsIntentScreen(event.context.getString(L10nR.string.ss_privacy_policy_url)))
            }
        }
        return when {
            isLoading -> State.Loading
            showConfirmAnonymousAuth -> State.ConfirmSignInAnonymously { event ->
                when (event) {
                    OverlayEvent.Confirm -> {
                        isLoading = true
                        scope.launch {
                            val isAuthenticated = authAnonymously().getOrElse { false }
                            if (isAuthenticated) {
                                navigator.resetRoot(HomeNavScreen)
                            } else {
                                showConfirmAnonymousAuth = false
                                onAuthError()
                            }
                        }
                    }

                    OverlayEvent.Dismiss -> showConfirmAnonymousAuth = false
                }
            }

            else -> State.Default(snackbarState, eventSink)
        }
    }

    private suspend fun authWithGoogle(context: Context): Result<Boolean> {
        return try {
            val response = credentialManager.getCredential(context, getCredentialRequest)
            val authenticated = handleCredentialResponse(response)
            Result.success(authenticated)
        } catch (ex: GetCredentialException) {
            Timber.e(ex)
            Result.failure(ex)
        } catch (ex: NoCredentialException) {
            Timber.e(ex)
            Result.failure(ex)
        }
    }

    private suspend fun handleCredentialResponse(response: GetCredentialResponse): Boolean {
        return (response.credential as? CustomCredential)?.let { credential ->
            if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                try {
                    val googleIdTokenCredential = GoogleIdTokenCredential
                        .createFrom(credential.data)
                    val token = googleIdTokenCredential.idToken
                    val signInResult = withContext(dispatcherProvider.default) { authRepository.signIn(token) }

                    signInResult.getOrNull() is AuthResponse.Authenticated

                } catch (e: GoogleIdTokenParsingException) {
                    Timber.e("Received an invalid google id token response - $e")
                    false
                }
            } else {
                Timber.e("Unexpected type of credential - ${credential.type}")
                false
            }
        } ?: run {
            Timber.e("Unexpected type of credential - ${response.credential.type}")
            false
        }
    }

    private suspend fun authAnonymously(): Result<Boolean> {
        val signInResult = withContext(dispatcherProvider.default) { authRepository.signIn() }
        return Result.success(signInResult.getOrNull() is AuthResponse.Authenticated)
    }

}
