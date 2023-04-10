package ss.circuit.helpers.navigator

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.cryart.sabbathschool.core.extensions.context.launchWebUrl
import com.cryart.sabbathschool.core.navigation.AppNavigator
import com.cryart.sabbathschool.core.navigation.Destination
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.Screen
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.parcelize.Parcelize

/**
 * Custom navigator that adds support for initiating navigation in standard Android.
 *
 * Extracted from [star sample](https://github.com/slackhq/circuit/blob/main/samples/star/src/main/kotlin/com/slack/circuit/star/navigator/AndroidSupportingNavigator.kt)
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
 *}
 *
 * ```
 * */
class AndroidSupportingNavigator @AssistedInject constructor(
    private val appNavigator: AppNavigator,
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
            is AndroidScreen.CustomTabsIntentScreen -> activity.launchWebUrl(screen.url)
            is AndroidScreen.IntentScreen -> activity.startActivity(screen.intent)
            is AndroidScreen.LegacyDestination -> appNavigator.navigate(activity, screen.destination, screen.extras)
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
    data class CustomTabsIntentScreen(val url: String) : AndroidScreen

    @Parcelize
    data class IntentScreen(val intent: Intent) : AndroidScreen

    @Parcelize
    data class LegacyDestination(
        val destination: Destination,
        val extras: Bundle? = null
    ) : AndroidScreen
}
