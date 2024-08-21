package app.ss.widgets.glance

import androidx.compose.runtime.Composable
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize

@Composable
internal fun WidgetLoading(modifier: GlanceModifier = GlanceModifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            modifier = GlanceModifier,
            color = GlanceTheme.colors.primary
        )
    }
}
