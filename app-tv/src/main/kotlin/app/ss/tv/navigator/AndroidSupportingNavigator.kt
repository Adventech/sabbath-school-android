/*
 * Copyright (c) 2023. Adventech <info@adventech.io>
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

package app.ss.tv.navigator

import android.content.Intent
import androidx.activity.ComponentActivity
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.screen.Screen
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.parcelize.Parcelize

class AndroidSupportingNavigator @AssistedInject constructor(
    @Assisted private val navigator: Navigator,
    @Assisted private val activity: ComponentActivity
) : Navigator by navigator {

    override fun goTo(screen: Screen) =
        when (screen) {
            is AndroidScreen -> goToAndroidScreen(screen)
            else -> navigator.goTo(screen)
        }

    private fun goToAndroidScreen(screen: AndroidScreen) {
        when (screen) {
            is AndroidScreen.IntentScreen -> activity.startActivity(screen.intent)
            AndroidScreen.Finish -> activity.finishAfterTransition()
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(
            navigator: Navigator,
            activity: ComponentActivity
        ): AndroidSupportingNavigator
    }
}

sealed interface AndroidScreen : Screen {

    @Parcelize
    data class IntentScreen(val intent: Intent) : AndroidScreen

    @Parcelize
    data object Finish : AndroidScreen
}
