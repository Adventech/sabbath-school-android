package ss.services.circuit.impl.navigator

import androidx.activity.ComponentActivity
import com.cryart.sabbathschool.core.extensions.context.launchWebUrl
import com.cryart.sabbathschool.core.navigation.AppNavigator
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.screen.Screen
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import ss.libraries.circuit.navigation.CustomTabsIntentScreen
import ss.libraries.circuit.navigation.LegacyDestination

/**
 * Custom navigator that adds support for initiating navigation in standard Android.
 *
 * Extracted from
 * [star sample](https://github.com/slackhq/circuit/blob/main/samples/star/src/main/kotlin/com/slack/circuit/star/navigator/AndroidSupportingNavigator.kt)
 *
 * Provide custom navigator like below:
 * ```kotlin
 *
 * @Inject lateinit var factory: AndroidSupportingNavigator.Factory
 *
 * val circuitNavigator = rememberCircuitNavigator(backstack)
 * val navigator =
 * remember(circuitNavigator) {
 *   factory.create(circuitNavigator, this/*activity*/)
 * }
 *
 * ```
 */
class AndroidSupportingNavigator
@AssistedInject
constructor(
    private val appNavigator: AppNavigator,
    @Assisted private val navigator: Navigator,
    @Assisted private val activity: ComponentActivity
) : Navigator by navigator {

  override fun goTo(screen: Screen) {
    when (screen) {
      is CustomTabsIntentScreen -> activity.launchWebUrl(screen.url)
      is LegacyDestination -> appNavigator.navigate(activity, screen.destination, screen.extras)
      else -> navigator.goTo(screen)
    }
  }

  @AssistedFactory
  interface Factory {
    fun create(navigator: Navigator, activity: ComponentActivity): AndroidSupportingNavigator
  }
}
