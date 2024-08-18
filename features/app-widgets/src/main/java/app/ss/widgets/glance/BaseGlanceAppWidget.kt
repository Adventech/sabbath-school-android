package app.ss.widgets.glance

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import app.ss.widgets.WidgetDataProvider
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface AppWidgetDataProviderEntryPoint {
    fun widgetDataProvider(): WidgetDataProvider
}

/**
 * A base [GlanceAppWidget] class that handles injection.
 */
internal abstract class BaseGlanceAppWidget : GlanceAppWidget() {

    fun dataProvider(context: Context): WidgetDataProvider {
        val appContext = context.applicationContext ?: throw IllegalStateException()
        val hiltEntryPoint =
            EntryPointAccessors.fromApplication(appContext, AppWidgetDataProviderEntryPoint::class.java)
        return hiltEntryPoint.widgetDataProvider()
    }
}
