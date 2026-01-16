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

package ss.services.circuit.impl.interceptor

import android.app.Activity
import com.cryart.sabbathschool.core.extensions.context.launchWebUrl
import com.cryart.sabbathschool.core.navigation.AppNavigator
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuitx.navigation.intercepting.InterceptedGoToResult
import com.slack.circuitx.navigation.intercepting.NavigationContext
import com.slack.circuitx.navigation.intercepting.NavigationInterceptor
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import ss.libraries.circuit.navigation.CustomTabsIntentScreen
import ss.libraries.circuit.navigation.LegacyDestination

/**
 * An interceptor for [CustomTabsIntentScreen] and [LegacyDestination].
 */
class AndroidSupportingInterceptor
@AssistedInject constructor(
    private val appNavigator: AppNavigator,
    @Assisted private val activity: Activity
) : NavigationInterceptor {

    override fun goTo(screen: Screen, navigationContext: NavigationContext): InterceptedGoToResult {
        return when (screen) {
            is CustomTabsIntentScreen -> {
                activity.launchWebUrl(screen.url)
                NavigationInterceptor.SuccessConsumed
            }

            is LegacyDestination -> {
                appNavigator.navigate(activity, screen.destination, screen.extras)
                NavigationInterceptor.SuccessConsumed
            }

            else -> NavigationInterceptor.Skipped
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(activity: Activity): AndroidSupportingInterceptor
    }
}
