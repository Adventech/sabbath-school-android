package ss.circuit.helpers.navigation

import android.os.Bundle
import com.cryart.sabbathschool.core.navigation.Destination
import com.slack.circuit.runtime.screen.Screen
import kotlinx.parcelize.Parcelize

/** A circuit screen for launching a legacy [Destination]. */
@Parcelize
data class LegacyDestination(
    val destination: Destination,
    val extras: Bundle? = null
) : Screen