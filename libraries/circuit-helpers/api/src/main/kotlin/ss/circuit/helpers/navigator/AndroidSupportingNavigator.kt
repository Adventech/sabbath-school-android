package ss.circuit.helpers.navigator

import android.content.Intent
import androidx.activity.ComponentActivity
import com.cryart.sabbathschool.core.extensions.context.launchWebUrl
import com.slack.circuit.Navigator
import com.slack.circuit.Screen
import kotlinx.parcelize.Parcelize

/**
 * Custom navigator that adds support for initiating navigation in standard Android.
 *
 * Extracted from [star sample](https://github.com/slackhq/circuit/blob/main/samples/star/src/main/kotlin/com/slack/circuit/star/navigator/AndroidSupportingNavigator.kt)
 *
 * Provide custom navigator like below:
 * ```kotlin
 *
 * val circuitNavigator = rememberCircuitNavigator(backstack)
 * val navigator =
 * remember(circuitNavigator) {
 *   AndroidSupportingNavigator(circuitNavigator, this/*activity*/)
 *}
 *
 * ```
 * */
class AndroidSupportingNavigator(
  private val navigator: Navigator,
  private val activity: ComponentActivity
) : Navigator by navigator {

  override fun goTo(screen: Screen) =
    when (screen) {
      is AndroidScreen -> goToAndroidScreen(screen)
      else -> navigator.goTo(screen)
    }

  private fun goToAndroidScreen(screen: AndroidScreen) {
    when (screen) {
      is AndroidScreen.CustomTabsIntentScreen -> activity.launchWebUrl(screen.url)
      is AndroidScreen.IntentScreen -> activity.startActivity(screen.intent)
    }
  }
}

sealed interface AndroidScreen : Screen {

    @Parcelize
    data class CustomTabsIntentScreen(val url: String) : AndroidScreen

    @Parcelize
    data class IntentScreen(val intent: Intent) : AndroidScreen
}
