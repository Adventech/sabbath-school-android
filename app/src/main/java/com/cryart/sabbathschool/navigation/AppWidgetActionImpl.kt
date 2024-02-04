package com.cryart.sabbathschool.navigation

import android.content.Context
import android.content.Intent
import app.ss.widgets.AppWidgetAction
import com.cryart.sabbathschool.lessons.ui.lessons.SSLessonsActivity
import com.cryart.sabbathschool.lessons.ui.readings.SSReadingActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppWidgetActionImpl @Inject constructor(@ApplicationContext private val appContext: Context) :
    AppWidgetAction {
  override fun launchLesson(quarterlyIndex: String): Intent =
      SSLessonsActivity.launchIntent(appContext, quarterlyIndex)

  override fun launchRead(lessonIndex: String, dayIndex: String?): Intent =
      SSReadingActivity.launchIntent(appContext, lessonIndex, dayIndex)
}
