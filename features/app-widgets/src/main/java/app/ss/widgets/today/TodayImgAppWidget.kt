package app.ss.widgets.today

import android.appwidget.AppWidgetManager
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.widget.RemoteViews
import app.ss.widgets.BaseWidgetProvider
import app.ss.widgets.R
import app.ss.widgets.clickIntent
import app.ss.widgets.extensions.RemoteViewsTarget
import app.ss.widgets.model.TodayWidgetModel
import app.ss.widgets.model.WidgetType
import coil.imageLoader
import coil.request.ImageRequest
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class TodayImgAppWidget : BaseWidgetProvider<TodayWidgetModel>() {

    override val type: WidgetType
        get() = WidgetType.TODAY_IMG

    override fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        model: TodayWidgetModel?
    ) {
        val views = RemoteViews(context.packageName, R.layout.today_app_widget_img)
        views.setTextViewText(R.id.widget_lesson_date, model?.date ?: context.getString(R.string.ss_widget_error_label))
        views.setTextViewText(R.id.widget_lesson_title, model?.title ?: context.getString(R.string.ss_widget_error_label))

        views.setOnClickPendingIntent(R.id.widget_root, model?.intent?.clickIntent(context))

        val request = ImageRequest.Builder(context)
            .data(model?.cover)
            .error(R.drawable.bg_img_placeholder)
            .placeholder(R.drawable.bg_img_placeholder)
            // .transformations(RoundedCornersTransformation(20f))
            .target(
                RemoteViewsTarget { drawable ->
                    val bitmap = (drawable as? BitmapDrawable)?.bitmap
                    views.setImageViewBitmap(R.id.widget_cover, bitmap)

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            )
            .build()

        context.imageLoader.enqueue(request)
    }
}
