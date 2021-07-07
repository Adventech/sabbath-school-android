package app.ss.widgets.today

import android.appwidget.AppWidgetManager
import android.content.Context
import android.widget.RemoteViews
import app.ss.widgets.BaseWidgetProvider
import app.ss.widgets.R
import app.ss.widgets.model.TodayWidgetModel
import app.ss.widgets.model.WidgetType
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
        views.setTextViewText(R.id.widget_lesson_date, model?.date ?: "----")
        views.setTextViewText(R.id.widget_lesson_title, model?.title ?: "----")

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
