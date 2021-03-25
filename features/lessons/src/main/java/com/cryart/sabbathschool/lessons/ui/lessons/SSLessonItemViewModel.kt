/*
 * Copyright (c) 2020 Adventech <info@adventech.io>
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
package com.cryart.sabbathschool.lessons.ui.lessons

import android.content.Context
import android.content.Intent
import androidx.databinding.BaseObservable
import com.cryart.sabbathschool.core.misc.SSConstants
import app.ss.lessons.data.model.SSLesson
import com.cryart.sabbathschool.lessons.ui.readings.SSReadingActivity
import com.cryart.sabbathschool.lessons.ui.viewmodel.SSViewModel
import org.joda.time.format.DateTimeFormat
import java.util.Locale

internal class SSLessonItemViewModel(
    private val context: Context,
    private var ssLesson: SSLesson
) : BaseObservable(), SSViewModel {

    val title: String get() = ssLesson.title
    val date: String
        get() {
            val startDateOut = DateTimeFormat.forPattern(SSConstants.SS_DATE_FORMAT_OUTPUT)
                .print(
                    DateTimeFormat.forPattern(SSConstants.SS_DATE_FORMAT)
                        .parseLocalDate(ssLesson.start_date)
                )

            val endDateOut = DateTimeFormat.forPattern(SSConstants.SS_DATE_FORMAT_OUTPUT)
                .print(
                    DateTimeFormat.forPattern(SSConstants.SS_DATE_FORMAT)
                        .parseLocalDate(ssLesson.end_date)
                )

            return "$startDateOut - $endDateOut".capitalize(Locale.getDefault())
        }

    fun setSsLesson(ssLesson: SSLesson) {
        this.ssLesson = ssLesson
        notifyChange()
    }

    fun onItemClick() {
        val ssReadingIntent = Intent(context, SSReadingActivity::class.java)
        ssReadingIntent.putExtra(SSConstants.SS_LESSON_INDEX_EXTRA, ssLesson.index)
        context.startActivity(ssReadingIntent)
    }

    override fun destroy() {}
}
