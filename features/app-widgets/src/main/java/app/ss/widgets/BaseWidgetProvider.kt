package app.ss.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import app.ss.widgets.model.WidgetType
import app.ss.widgets.today.TodayAppWidget
import app.ss.widgets.today.TodayImgAppWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

abstract class BaseWidgetProvider<M> : AppWidgetProvider() {

    internal abstract val type: WidgetType

    abstract fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        model: M?
    )

    @Inject
    internal lateinit var dataProvider: WidgetDataProvider

    private val job = SupervisorJob()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + job)

    override fun onReceive(context: Context, widgetIntent: Intent) {
        val action = widgetIntent.action
        if (REFRESH_ACTION == action) {
            Timber.d("received REFRESH_ACTION from widget")

            // Instruct the widget manager to update the widget
            val mgr = AppWidgetManager.getInstance(context)
            val clazz = when (type) {
                WidgetType.TODAY -> TodayAppWidget::class.java
                WidgetType.TODAY_IMG -> TodayImgAppWidget::class.java
            }
            val cn = ComponentName(context, clazz)
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
            val model = when (type) {
                WidgetType.TODAY,
                WidgetType.TODAY_IMG -> dataProvider.getTodayModel()
            }

            withContext(Dispatchers.Main) {
                for (appWidgetId in appWidgetIds) {
                    updateAppWidget(context, appWidgetManager, appWidgetId, model as? M)
                }
            }
        }
    }

    companion object {
        private const val REFRESH_ACTION = "app.ss.appwidget.action.REFRESH"
    }
}
