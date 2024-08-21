package app.ss.widgets.model

import android.content.Intent

sealed interface WeekWidgetState {

    data object Loading : WeekWidgetState
    data object Error : WeekWidgetState
    data class Success(
        val model: WeekModel,
        val lessonIntent: Intent,
    ) : WeekWidgetState
}
