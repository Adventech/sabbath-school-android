package ss.settings.ui

import androidx.compose.runtime.Immutable
import app.ss.design.compose.extensions.list.ListEntity
import ss.settings.ui.prefs.PrefListEntity

/**
 * A sealed interface representing the different types of entities that can be displayed on the settings screen.
 * This is used to group preferences into sections.
 */
@Immutable
sealed interface SettingsScreenEntity {
    val key: String

    /**
     * A single preference item.
     */
    data class Item(
        val pref: PrefListEntity,
    ) : SettingsScreenEntity {
        override val key: String = pref.id
    }

    /**
     * A group of preference items.
     */
    data class Group(
        val prefs: List<PrefListEntity>,
    ) : SettingsScreenEntity {
        // A unique key for the group
        override val key: String = prefs.joinToString { it.id }
    }
}

/**
 * Transforms a flat list of [ListEntity] into a grouped list of [SettingsScreenEntity].
 * It groups items that appear after a [PrefListEntity.Section] into a [SettingsScreenEntity.Group].
 */
internal fun List<ListEntity>.toGroupedEntities(): List<SettingsScreenEntity> {
    val groupedEntities = mutableListOf<SettingsScreenEntity>()
    val currentGroupItems = mutableListOf<PrefListEntity>()

    this.forEach { entity ->
        if (entity is PrefListEntity.Section) {
            // If we have items in the current group, add them to the list before starting a new section.
            if (currentGroupItems.isNotEmpty()) {
                groupedEntities.add(SettingsScreenEntity.Group(prefs = currentGroupItems.toList()))
                currentGroupItems.clear()
            }
            // Add the section header itself as a standalone item.
            groupedEntities.add(SettingsScreenEntity.Item(pref = entity))
        } else if (entity is PrefListEntity) {
            // Add any other preference type to the current group.
            currentGroupItems.add(entity)
        }
    }

    // Add any remaining items in the last group.
    if (currentGroupItems.isNotEmpty()) {
        groupedEntities.add(SettingsScreenEntity.Group(prefs = currentGroupItems.toList()))
    }

    return groupedEntities
}
