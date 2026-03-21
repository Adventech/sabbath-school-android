/*
 * Copyright (c) 2026. Adventech <info@adventech.io>
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

package ss.navigation.suite

import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuitx.navigation.intercepting.InterceptedResetRootResult
import com.slack.circuitx.navigation.intercepting.NavigationContext
import com.slack.circuitx.navigation.intercepting.NavigationInterceptor
import ss.libraries.circuit.navigation.HomeNavScreen
import ss.libraries.circuit.navigation.LoginScreen

/**
 * A [NavigationInterceptor] that intercepts requests to reset the root to specific "sentinel" screens.
 *
 * Instead of performing a standard navigation reset within the current context, this interceptor
 * captures requests for [LoginScreen] and [HomeNavScreen] and delegates them to the [onReset] callback.
 *
 * This pattern allows nested navigators to signal high-level app state changes (such as logging out
 * or resetting the main dashboard) by simply attempting to navigate to those screens.
 *
 * @property onReset A callback invoked when a matching root screen is intercepted. The target [Screen]
 * is passed as an argument to distinguish the requested action.
 */
class RootScreenInterceptor(
    private val onReset: (Screen) -> Unit,
) : NavigationInterceptor {

    override fun resetRoot(
        newRoot: Screen,
        options: Navigator.StateOptions,
        navigationContext: NavigationContext,
    ): InterceptedResetRootResult {
        return when (newRoot) {
            LoginScreen, HomeNavScreen -> {
                onReset(newRoot)
                NavigationInterceptor.SuccessConsumed
            }

            else -> NavigationInterceptor.Skipped
        }
    }
}
