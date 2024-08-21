package app.ss.widgets.model

import android.content.Intent
import androidx.compose.runtime.Immutable
import app.ss.widgets.glance.extensions.fallbackIntent

sealed interface TodayWidgetState {

    val launchIntent: Intent

    @Immutable
    data object Loading : TodayWidgetState {
        override val launchIntent: Intent = fallbackIntent
    }

    @Immutable
    data object Error : TodayWidgetState {
        override val launchIntent: Intent = fallbackIntent
    }

    @Immutable
    data class Success(
        val model: TodayModel,
       override val launchIntent: Intent,
    ) : TodayWidgetState
}
