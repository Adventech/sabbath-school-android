package ss.libraries.circuit.navigation

import com.slack.circuit.runtime.screen.Screen
import kotlinx.parcelize.Parcelize

/** A circuit [Screen] for launching a chrome custom tab with the provided [url]. */
@Parcelize
data class CustomTabsIntentScreen(val url: String) : Screen