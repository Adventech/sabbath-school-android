package app.ss.widgets.today

import android.appwidget.AppWidgetManager
import android.content.Context
import android.widget.RemoteViews
import app.ss.widgets.BaseWidgetProvider
import app.ss.widgets.clickIntent
import app.ss.widgets.model.TodayWidgetModel
import app.ss.widgets.model.WidgetType
import dagger.hilt.android.AndroidEntryPoint
import app.ss.translations.R as L10nR
import app.ss.widgets.R as WidgetsR

/**
 * Implementation of Today App Widget functionality.
 */
@AndroidEntryPoint
internal class TodayAppWidget : BaseWidgetProvider<TodayWidgetModel>() {

    override val type: WidgetType
        get() = WidgetType.TODAY

    override fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        model: TodayWidgetModel?
    ) {
        val views = RemoteViews(context.packageName, WidgetsR.layout.today_app_widget)
        views.setTextViewText(WidgetsR.id.widget_lesson_date, model?.date ?: context.getString(L10nR.string.ss_widget_error_label))
        views.setTextViewText(WidgetsR.id.widget_lesson_title, model?.title ?: context.getString(L10nR.string.ss_widget_error_label))

        views.setOnClickPendingIntent(WidgetsR.id.widget_root, model?.intent?.clickIntent(context))

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
