package ss.libraries.storage.api.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.ss.models.AppWidgetDay

@Entity(tableName = "app_widget")
data class AppWidgetEntity(
    @PrimaryKey
    val quarterlyIndex: String,
    val cover: String,
    val title: String,
    val description: String,
    val days: List<AppWidgetDay>,
)
