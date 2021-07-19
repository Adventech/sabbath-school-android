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
import android.graphics.Color
import android.view.View
import androidx.databinding.ObservableInt
import app.ss.lessons.data.model.SSQuarterlyInfo
import app.ss.widgets.AppWidgetHelper
import com.cryart.sabbathschool.core.extensions.prefs.SSPrefs
import com.cryart.sabbathschool.core.misc.SSConstants
import com.cryart.sabbathschool.lessons.ui.readings.SSReadingActivity
import com.cryart.sabbathschool.lessons.ui.viewmodel.SSViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.joda.time.DateTime
import org.joda.time.Interval
import org.joda.time.format.DateTimeFormat

internal class SSLessonsViewModel(
    private val context: Context,
    private val ssPrefs: SSPrefs,
    private var dataListener: DataListener?,
    private var ssQuarterlyIndex: String,
    private val appWidgetHelper: AppWidgetHelper
) : SSViewModel {

    private val mDatabase: DatabaseReference = FirebaseDatabase.getInstance().reference

    var ssQuarterlyInfo: SSQuarterlyInfo? = null

    val date: String get() = ssQuarterlyInfo?.quarterly?.human_date ?: ""
    val description: String get() = ssQuarterlyInfo?.quarterly?.description ?: ""
    val cover: String get() = ssQuarterlyInfo?.quarterly?.cover ?: ""
    val primaryColor: Int
        get() = Color.parseColor(
            ssQuarterlyInfo?.quarterly?.color_primary ?: "#03000000"
        )

    var ssLessonsLoadingVisibility: ObservableInt
    var ssLessonsEmptyStateVisibility: ObservableInt
    var ssLessonsErrorStateVisibility: ObservableInt
    var ssLessonsCoordinatorVisibility: ObservableInt

    init {
        mDatabase.keepSynced(true)
        ssLessonsLoadingVisibility = ObservableInt(View.INVISIBLE)
        ssLessonsEmptyStateVisibility = ObservableInt(View.INVISIBLE)
        ssLessonsErrorStateVisibility = ObservableInt(View.INVISIBLE)
        ssLessonsCoordinatorVisibility = ObservableInt(View.INVISIBLE)
        loadQuarterlyInfo(true)
    }

    fun setSsQuarterlyIndex(ssQuarterlyIndex: String) {
        this.ssQuarterlyIndex = ssQuarterlyIndex
        loadQuarterlyInfo(false)
    }

    private fun loadQuarterlyInfo(showLoading: Boolean) {
        if (showLoading) {
            ssLessonsLoadingVisibility.set(View.VISIBLE)
        }
        ssLessonsEmptyStateVisibility.set(View.INVISIBLE)
        ssLessonsErrorStateVisibility.set(View.INVISIBLE)
        mDatabase.child(SSConstants.SS_FIREBASE_QUARTERLY_INFO_DATABASE)
            .child(ssQuarterlyIndex)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    ssQuarterlyInfo = SSQuarterlyInfo(dataSnapshot)

                    ssQuarterlyInfo?.let {
                        ssPrefs.setLastQuarterlyIndex(it.quarterly.index)
                        appWidgetHelper.refreshAll()

                        dataListener?.onQuarterlyChanged(it)
                    }

                    ssLessonsLoadingVisibility.set(View.INVISIBLE)
                    ssLessonsEmptyStateVisibility.set(View.INVISIBLE)
                    ssLessonsErrorStateVisibility.set(View.INVISIBLE)
                    ssLessonsCoordinatorVisibility.set(View.VISIBLE)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    ssLessonsLoadingVisibility.set(View.INVISIBLE)
                    ssLessonsEmptyStateVisibility.set(View.INVISIBLE)
                    ssLessonsCoordinatorVisibility.set(View.INVISIBLE)
                    ssLessonsErrorStateVisibility.set(View.VISIBLE)
                }
            })
    }

    override fun destroy() {
        dataListener = null
        ssQuarterlyInfo = null
    }

    fun onReadClick() {
        if (ssQuarterlyInfo?.lessons?.isNotEmpty() == true) {
            val today = DateTime.now()
            var ssLessonIndex = ssQuarterlyInfo!!.lessons[0].index
            for (ssLesson in ssQuarterlyInfo!!.lessons) {
                val startDate = DateTimeFormat.forPattern(SSConstants.SS_DATE_FORMAT)
                    .parseLocalDate(ssLesson.start_date).toDateTimeAtStartOfDay()
                val endDate = DateTimeFormat.forPattern(SSConstants.SS_DATE_FORMAT)
                    .parseLocalDate(ssLesson.end_date)
                    .plusDays(1)
                    .toDateTimeAtStartOfDay()
                    .plusHours(12)
                if (startDate.isBefore(endDate) && Interval(startDate, endDate).contains(today)) {
                    ssLessonIndex = ssLesson.index
                    break
                }
            }

            val ssReadingIntent = SSReadingActivity.launchIntent(context, ssLessonIndex)
            context.startActivity(ssReadingIntent)
        }
    }

    fun setDataListener(dataListener: DataListener) {
        this.dataListener = dataListener
    }

    interface DataListener {
        fun onQuarterlyChanged(ssQuarterlyInfo: SSQuarterlyInfo)
    }
}
