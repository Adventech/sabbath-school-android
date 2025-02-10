package com.cryart.sabbathschool.navigation

import android.content.Context
import android.content.Intent
import android.os.Build
import app.ss.widgets.AppWidgetAction
import com.cryart.sabbathschool.core.navigation.AppNavigator
import dagger.hilt.android.qualifiers.ApplicationContext
import ss.libraries.circuit.navigation.DocumentScreen
import ss.libraries.circuit.navigation.ResourceScreen
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppWidgetActionImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val appNavigator: AppNavigator,
) : AppWidgetAction {

    override fun launchLesson(quarterlyIndex: String): Intent =
        appNavigator.screenIntent(
            appContext, ResourceScreen(quarterlyIndex.toResourceIndex())
        )

    override fun launchRead(lessonIndex: String, dayIndex: String?): Intent =
        appNavigator.screenIntent(
            appContext,
            DocumentScreen(index = lessonIndex.toDocumentIndex(), dayIndex)
        ).apply {
            // See [Intent.filterEquals].
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                identifier = "$lessonIndex/${dayIndex.orEmpty()}"
            }
        }
}
