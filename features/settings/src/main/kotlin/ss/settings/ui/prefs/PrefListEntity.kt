package ss.settings.ui.prefs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import app.ss.design.compose.extensions.content.ContentSpec
import app.ss.design.compose.extensions.list.ListEntity
import app.ss.design.compose.widget.icon.IconSlot

/**
 * A sealed interface representing different types of preference items that can be displayed on the settings screen.
 */
sealed interface PrefListEntity : ListEntity {

    /**
     * A generic preference item with a title, summary, icon, and an action.
     */
    @Immutable
    data class Generic(
        override val id: String,
        override val enabled: Boolean = true,
        val title: ContentSpec,
        val icon: IconSlot? = null,
        val summary: ContentSpec? = null,
        val onClick: () -> Unit,
        val withWarning: Boolean = false
    ) : PrefListEntity {
        @Composable
        override fun Content() = GenericPreference(item = this)
    }

    /**
     * A section header to group related preferences.
     */
    @Immutable
    data class Section(
        val title: ContentSpec,
        override val id: String,
        override val enabled: Boolean = true
    ) : PrefListEntity {
        @Composable
        override fun Content() = SectionHeader(item = this)
    }

    /**
     * A preference item with a switch to toggle a setting on or off.
     */
    @Immutable
    data class Switch(
        override val id: String,
        override val enabled: Boolean = true,
        val title: ContentSpec,
        val checked: Boolean,
        val onCheckChanged: (Boolean) -> Unit,
        val icon: IconSlot? = null,
        val summary: ContentSpec? = null,
    ) : PrefListEntity {
        @Composable
        override fun Content() = SwitchPreference(item = this)
    }
}
