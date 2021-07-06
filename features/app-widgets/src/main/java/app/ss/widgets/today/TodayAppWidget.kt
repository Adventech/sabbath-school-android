package app.ss.widgets.today

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import app.ss.lessons.data.model.TodayModel
import app.ss.widgets.R
import app.ss.widgets.WidgetDataProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

/**
 * Implementation of Today App Widget functionality.
 */
@AndroidEntryPoint
class TodayAppWidget : AppWidgetProvider() {

    @Inject
    lateinit var dataProvider: WidgetDataProvider

    private val job = SupervisorJob()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + job)

    override fun onReceive(context: Context, widgetIntent: Intent) {
        val action = widgetIntent.action
        if (REFRESH_ACTION == action) {
            Timber.d("received REFRESH_ACTION from widget")

            // Instruct the widget manager to update the widget
            val mgr = AppWidgetManager.getInstance(context)
            val cn = ComponentName(context, TodayAppWidget::class.java)
            val ids = mgr.getAppWidgetIds(cn)
            onUpdate(context, mgr, ids)
        } else {
            super.onReceive(context, widgetIntent)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        coroutineScope.launch {
            val model = dataProvider.getTodayModel()

            withContext(Dispatchers.Main) {
                for (appWidgetId in appWidgetIds) {
                    updateAppWidget(context, appWidgetManager, appWidgetId, model)
                }
            }
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        model: TodayModel?
    ) {
        val views = RemoteViews(context.packageName, R.layout.today_app_widget)
        views.setTextViewText(R.id.widget_lesson_date, model?.date ?: "----")
        views.setTextViewText(R.id.widget_lesson_title, model?.title ?: "----")

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    companion object {
        private const val REFRESH_ACTION = "app.ss.appwidget.action.REFRESH"
    }
}
