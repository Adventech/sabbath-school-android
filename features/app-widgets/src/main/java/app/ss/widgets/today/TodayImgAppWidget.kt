package app.ss.widgets.today

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.widget.RemoteViews
import app.ss.widgets.BaseWidgetProvider
import app.ss.widgets.R
import app.ss.widgets.extensions.RemoteViewsTarget
import app.ss.widgets.model.TodayWidgetModel
import app.ss.widgets.model.WidgetType
import coil.imageLoader
import coil.request.ImageRequest
import com.cryart.sabbathschool.core.misc.SSConstants
import com.cryart.sabbathschool.core.navigation.Destination
import com.cryart.sabbathschool.core.navigation.toUri
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TodayImgAppWidget : BaseWidgetProvider<TodayWidgetModel>() {

    override val type: WidgetType
        get() = WidgetType.TODAY_IMG

    override fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        model: TodayWidgetModel?
    ) {
        val views = RemoteViews(context.packageName, R.layout.today_app_widget_img)
        views.setTextViewText(R.id.widget_lesson_date, model?.date ?: "----")
        views.setTextViewText(R.id.widget_lesson_title, model?.title ?: "----")

        val pendingIntent: PendingIntent? = model?.let {
            Intent().apply {
                data = Destination.READ.toUri(SSConstants.SS_LESSON_INDEX_EXTRA to model.lessonIndex)
            }.let { intent ->
                PendingIntent.getActivity(context, 0, intent, 0)
            }
        }
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

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
