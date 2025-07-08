
package ss.settings.ui.prefs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A composable that displays a basic preference row with an icon, title, summary, and an action.
 * This is a building block for creating more complex preference items.
 *
 * @param modifier The modifier to be applied to the row.
 * @param icon An optional icon to be displayed at the start of the row.
 * @param title The main title of the preference.
 * @param summary An optional summary to be displayed below the title.
 * @param action An optional action to be displayed at the end of the row, such as a switch or a checkbox.
 */
@Composable
internal fun PreferenceRow(
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null,
    title: @Composable () -> Unit,
    summary: @Composable (() -> Unit)? = null,
    action: @Composable (RowScope.() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon?.invoke()

        Column(
            modifier = Modifier.weight(1.0f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            title()
            summary?.invoke()
        }

        action?.invoke(this)
    }
}
