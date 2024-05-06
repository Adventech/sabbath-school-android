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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.components.SingletonComponent
import ss.libraries.circuit.navigation.CustomTabsIntentScreen
import ss.libraries.circuit.navigation.LoginScreen
import timber.log.Timber

const val PRIVACY_POLICY_URL = "https://adventech.io/privacy-policy"

class LoginPresenter @AssistedInject constructor(
    @Assisted private val navigator: Navigator,
) : Presenter<State> {

    @CircuitInject(LoginScreen::class, SingletonComponent::class)
    @AssistedFactory
    interface Factory {
        fun create(navigator: Navigator): LoginPresenter
    }

    @Composable
    override fun present(): State {
        var isLoading by rememberRetained { mutableStateOf(false) }
        var isAnonymously by rememberRetained { mutableStateOf(false) }

        val eventSink: (Event) -> Unit = { event ->
            when (event) {
                Event.SignInAnonymously -> {
                    Timber.i("Sign in Anonymously")
                    isAnonymously = true
                }

                Event.SignInWithGoogle -> {
                    Timber.i("Sign in with Google")
                }

                Event.OpenPrivacyPolicy -> navigator.goTo(CustomTabsIntentScreen(PRIVACY_POLICY_URL))
            }
        }
        return when {
            isLoading -> State.Loading
            isAnonymously -> State.ConfirmSignInAnonymously(
                onConfirm = { isLoading = true },
                onDecline = { isAnonymously = false }
            )

            else -> State.Default(eventSink)
        }
    }
}
