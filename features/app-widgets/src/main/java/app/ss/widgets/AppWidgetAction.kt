package app.ss.widgets

import android.content.Intent

interface AppWidgetAction {

    fun launchLesson(quarterlyIndex: String): Intent

    fun launchRead(lessonIndex: String, dayIndex: String? = null): Intent
}