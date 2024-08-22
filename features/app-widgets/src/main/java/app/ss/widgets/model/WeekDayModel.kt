package app.ss.widgets.model

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.runtime.Immutable

@Immutable
data class WeekDayModel(
    val title: String,
    val date: String,
    val cover: String,
    val image: Bitmap?,
    val today: Boolean,
    val intent: Intent,
)
