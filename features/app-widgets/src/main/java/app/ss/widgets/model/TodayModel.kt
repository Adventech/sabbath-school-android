package app.ss.widgets.model

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable

@Immutable
data class TodayModel(
    val title: String,
    val date: String,
    val image: Bitmap?,
)
