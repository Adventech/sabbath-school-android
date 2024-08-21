package app.ss.widgets.model

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class WeekModel(
    val quarterlyIndex: String,
    val cover: Bitmap?,
    val title: String,
    val description: String,
    val days: ImmutableList<TodayModel>,
)
