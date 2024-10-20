package ss.libraries.circuit.navigation

import com.slack.circuit.runtime.screen.Screen
import kotlinx.parcelize.Parcelize

/** A circuit [Screen] for opening the settings screen. */
@Parcelize data class SettingsScreen(val showNavigation: Boolean) : Screen
