/*
 * Copyright (c) 2021. Adventech <info@adventech.io>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package app.ss.widgets.week

import android.appwidget.AppWidgetManager
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.text.Spanned
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import app.ss.widgets.BaseWidgetProvider
import app.ss.widgets.R
import app.ss.widgets.clickIntent
import app.ss.widgets.extensions.RemoteViewsTarget
import app.ss.widgets.model.WeekDayWidgetModel
import app.ss.widgets.model.WeekLessonWidgetModel
import app.ss.widgets.model.WidgetType
import coil.imageLoader
import coil.request.ImageRequest
import coil.transform.RoundedCornersTransformation
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class WeekLessonWidget : BaseWidgetProvider<WeekLessonWidgetModel>() {

    override val type: WidgetType
        get() = WidgetType.WEEK_LESSON

    override fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        model: WeekLessonWidgetModel?
    ) {

        val views = RemoteViews(context.packageName, R.layout.week_lesson_app_widget)
        views.setTextViewText(R.id.widget_quarterly_title, model?.quarterlyTitle ?: context.getString(R.string.ss_widget_error_label))
        views.setTextViewText(R.id.widget_lesson_title, model?.lessonTitle ?: context.getString(R.string.ss_widget_error_label))
        views.setOnClickPendingIntent(R.id.widget_root, model?.uri?.clickIntent(context))

        setWeekData(context, views, model)

        val request = ImageRequest.Builder(context)
            .data(model?.cover)
            .error(R.drawable.bg_img_placeholder)
            .placeholder(R.drawable.bg_img_placeholder)
            .size(
                context.resources.getDimensionPixelSize(R.dimen.ss_widget_book_cover_width),
                context.resources.getDimensionPixelSize(R.dimen.ss_widget_book_cover_height)
            )
            .transformations(RoundedCornersTransformation(12f))
            .target(
                RemoteViewsTarget { drawable ->
                    val bitmap = (drawable as? BitmapDrawable)?.bitmap
                    views.setImageViewBitmap(R.id.widget_lesson_cover, bitmap)

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            )
            .build()

        context.imageLoader.enqueue(request)
    }

    private fun setWeekData(context: Context, views: RemoteViews, model: WeekLessonWidgetModel?) {
        val default = context.getString(R.string.ss_widget_error_label)

        val colorTextDefault = ContextCompat.getColor(context, R.color.text_secondary)
        val colorTextPrimary = ContextCompat.getColor(context, R.color.text_primary)

        var day = model?.days?.getOrNull(0)
        views.setTextViewText(R.id.widget_day_one, day?.formattedTitle() ?: default)
        views.setTextViewText(R.id.widget_day_one_date, day?.date ?: default)
        views.setTextColor(R.id.widget_day_one, if (day?.today == true) colorTextPrimary else colorTextDefault)
        views.setTextColor(R.id.widget_day_one_date, if (day?.today == true) colorTextPrimary else colorTextDefault)
        views.setOnClickPendingIntent(R.id.widget_day_one_container, day?.uri?.clickIntent(context))

        day = model?.days?.getOrNull(1)
        views.setTextViewText(R.id.widget_day_two, day?.formattedTitle() ?: default)
        views.setTextViewText(R.id.widget_day_two_date, day?.date ?: default)
        views.setTextColor(R.id.widget_day_two, if (day?.today == true) colorTextPrimary else colorTextDefault)
        views.setTextColor(R.id.widget_day_two_date, if (day?.today == true) colorTextPrimary else colorTextDefault)
        views.setOnClickPendingIntent(R.id.widget_day_two_container, day?.uri?.clickIntent(context))

        day = model?.days?.getOrNull(2)
        views.setTextViewText(R.id.widget_day_three, day?.formattedTitle() ?: default)
        views.setTextViewText(R.id.widget_day_three_date, day?.date ?: default)
        views.setTextColor(R.id.widget_day_three, if (day?.today == true) colorTextPrimary else colorTextDefault)
        views.setTextColor(R.id.widget_day_three_date, if (day?.today == true) colorTextPrimary else colorTextDefault)
        views.setOnClickPendingIntent(R.id.widget_day_three_container, day?.uri?.clickIntent(context))

        day = model?.days?.getOrNull(3)
        views.setTextViewText(R.id.widget_day_four, day?.formattedTitle() ?: default)
        views.setTextViewText(R.id.widget_day_four_date, day?.date ?: default)
        views.setTextColor(R.id.widget_day_four, if (day?.today == true) colorTextPrimary else colorTextDefault)
        views.setTextColor(R.id.widget_day_four_date, if (day?.today == true) colorTextPrimary else colorTextDefault)
        views.setOnClickPendingIntent(R.id.widget_day_four_container, day?.uri?.clickIntent(context))

        day = model?.days?.getOrNull(4)
        views.setTextViewText(R.id.widget_day_five, day?.formattedTitle() ?: default)
        views.setTextViewText(R.id.widget_day_five_date, day?.date ?: default)
        views.setTextColor(R.id.widget_day_five, if (day?.today == true) colorTextPrimary else colorTextDefault)
        views.setTextColor(R.id.widget_day_five_date, if (day?.today == true) colorTextPrimary else colorTextDefault)
        views.setOnClickPendingIntent(R.id.widget_day_five_container, day?.uri?.clickIntent(context))

        day = model?.days?.getOrNull(5)
        views.setTextViewText(R.id.widget_day_six, day?.formattedTitle() ?: default)
        views.setTextViewText(R.id.widget_day_six_date, day?.date ?: default)
        views.setTextColor(R.id.widget_day_six, if (day?.today == true) colorTextPrimary else colorTextDefault)
        views.setTextColor(R.id.widget_day_six_date, if (day?.today == true) colorTextPrimary else colorTextDefault)
        views.setOnClickPendingIntent(R.id.widget_day_six_container, day?.uri?.clickIntent(context))

        day = model?.days?.getOrNull(6)
        views.setTextViewText(R.id.widget_day_seven, day?.formattedTitle() ?: default)
        views.setTextViewText(R.id.widget_day_seven_date, day?.date ?: default)
        views.setTextColor(R.id.widget_day_seven, if (day?.today == true) colorTextPrimary else colorTextDefault)
        views.setTextColor(R.id.widget_day_seven_date, if (day?.today == true) colorTextPrimary else colorTextDefault)
        views.setOnClickPendingIntent(R.id.widget_day_seven_container, day?.uri?.clickIntent(context))
    }

    private fun WeekDayWidgetModel.formattedTitle(): Spanned = buildSpannedString {
        if (today) {
            bold {
                append(title)
            }
        } else {
            append(title)
        }
    }
}
