package ss.circuit.helpers.impl

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
import javax.inject.Inject
import ss.circuit.helpers.impl.navigator.AndroidSupportingNavigator

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

        val screen = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(ARG_EXTRA_SCREEN, Screen::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(ARG_EXTRA_SCREEN)
        }) ?: run {
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
            val intent = Intent(context, CircuitActivity::class.java).apply {
                putExtra(ARG_EXTRA_SCREEN, screen)
            }
            context.startActivity(intent)
        }
    }
}