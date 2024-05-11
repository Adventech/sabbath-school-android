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

package ss.services.circuit.impl

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.os.BundleCompat
import app.ss.design.compose.theme.SsTheme
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.rememberCircuitNavigator
import com.slack.circuit.overlay.ContentWithOverlays
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuitx.android.rememberAndroidScreenAwareNavigator
import com.slack.circuitx.gesturenavigation.GestureNavigationDecoration
import dagger.hilt.android.AndroidEntryPoint
import ss.services.circuit.impl.navigator.AndroidSupportingNavigator
import javax.inject.Inject

@AndroidEntryPoint
class CircuitActivity : ComponentActivity() {

    @Inject
    lateinit var circuit: Circuit

    @Inject
    lateinit var supportingNavigatorFactory: AndroidSupportingNavigator.Factory

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val screen = intent.extras?.let {
            BundleCompat.getParcelable(it, ARG_EXTRA_SCREEN, Screen::class.java)
        } ?: run {
            finish()
            return
        }

        setContent {
            val windowSizeClass = calculateWindowSizeClass(activity = this)

            SsTheme(windowWidthSizeClass = windowSizeClass.widthSizeClass) {
                val backstack = rememberSaveableBackStack(screen)
                val circuitNavigator = rememberCircuitNavigator(backstack)
                val supportingNavigator = remember(circuitNavigator) {
                    supportingNavigatorFactory.create(circuitNavigator, this)
                }
                val navigator = rememberAndroidScreenAwareNavigator(supportingNavigator, this)
                ContentWithOverlays {
                    NavigableCircuitContent(
                        navigator,
                        backstack,
                        Modifier,
                        circuit,
                        decoration = GestureNavigationDecoration {
                            navigator.pop()
                        }
                    )
                }
            }
        }
    }

    companion object {
        private const val ARG_EXTRA_SCREEN = "extra_screen"

        fun launch(context: Context, screen: Screen) {
            context.startActivity(launchIntent(context, screen))
        }

        fun launchIntent(context: Context, screen: Screen) = Intent(context, CircuitActivity::class.java).apply {
            putExtra(ARG_EXTRA_SCREEN, screen)
        }
    }
}
