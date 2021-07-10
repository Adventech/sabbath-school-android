package app.ss.widgets.today

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import app.ss.widgets.BaseWidgetProvider
import app.ss.widgets.R
import app.ss.widgets.model.TodayWidgetModel
import app.ss.widgets.model.WidgetType
import com.cryart.sabbathschool.core.misc.SSConstants
import com.cryart.sabbathschool.core.navigation.Destination
import com.cryart.sabbathschool.core.navigation.toUri
import dagger.hilt.android.AndroidEntryPoint

/**
 * Implementation of Today App Widget functionality.
 */
@AndroidEntryPoint
class TodayAppWidget : BaseWidgetProvider<TodayWidgetModel>() {

    override val type: WidgetType
        get() = WidgetType.TODAY

    override fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        model: TodayWidgetModel?
    ) {
        val views = RemoteViews(context.packageName, R.layout.today_app_widget)
        views.setTextViewText(R.id.widget_lesson_date, model?.date ?: context.getString(R.string.ss_widget_error_label))
        views.setTextViewText(R.id.widget_lesson_title, model?.title ?: context.getString(R.string.ss_widget_error_label))

        val pendingIntent: PendingIntent? = model?.let {
            Intent().apply {
                data = Destination.READ.toUri(SSConstants.SS_LESSON_INDEX_EXTRA to model.lessonIndex)
            }.let { intent ->
                PendingIntent.getActivity(context, 0, intent, 0)
            }
        }
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
