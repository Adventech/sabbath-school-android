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
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.view.View
import android.widget.ImageView
import androidx.core.view.ViewCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableInt
import coil.load
import com.cryart.sabbathschool.core.misc.SSConstants
import com.cryart.sabbathschool.lessons.data.model.SSQuarterlyInfo
import com.cryart.sabbathschool.lessons.ui.viewmodel.SSViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.joda.time.DateTime
import org.joda.time.Interval
import org.joda.time.format.DateTimeFormat

class SSLessonsViewModel(
    context: Context?,
    private var dataListener: DataListener?,
    private var ssQuarterlyIndex: String?
) : SSViewModel {

    private val mDatabase: DatabaseReference = FirebaseDatabase.getInstance().reference

    var ssQuarterlyInfo: SSQuarterlyInfo? = null

    @JvmField
    var ssLessonsLoadingVisibility: ObservableInt
    @JvmField
    var ssLessonsEmptyStateVisibility: ObservableInt
    @JvmField
    var ssLessonsErrorStateVisibility: ObservableInt
    @JvmField
    var ssLessonsCoordinatorVisibility: ObservableInt
    private var shared: SharedPreferences?

    init {
        mDatabase.keepSynced(true)
        ssLessonsLoadingVisibility = ObservableInt(View.INVISIBLE)
        ssLessonsEmptyStateVisibility = ObservableInt(View.INVISIBLE)
        ssLessonsErrorStateVisibility = ObservableInt(View.INVISIBLE)
        ssLessonsCoordinatorVisibility = ObservableInt(View.INVISIBLE)
        shared = PreferenceManager.getDefaultSharedPreferences(context)
        loadQuarterlyInfo(true)
    }

    fun setSsQuarterlyIndex(ssQuarterlyIndex: String?) {
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
            .child(ssQuarterlyIndex!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    ssQuarterlyInfo = dataSnapshot.getValue(SSQuarterlyInfo::class.java)
                    if (shared != null) {
                        val editor = shared!!.edit()
                        editor.putString(
                            SSConstants.SS_LAST_QUARTERLY_INDEX,
                            ssQuarterlyInfo!!.quarterly.index
                        )
                        editor.apply()
                    }
                    if (dataListener != null) dataListener!!.onQuarterlyChanged(ssQuarterlyInfo!!)
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
        ssQuarterlyIndex = null
        shared = null
    }

    fun onReadClick() {
        if (ssQuarterlyInfo != null && ssQuarterlyInfo!!.lessons.size > 0) {
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

            // TODO: Launch SSReadingActivity
//            Intent ssReadingIntent = new Intent(context, SSReadingActivity.class);
//            ssReadingIntent.putExtra(SSConstants.SS_LESSON_INDEX_EXTRA, ssLessonIndex);
//            context.startActivity(ssReadingIntent);
        }
    }

    fun setDataListener(dataListener: DataListener?) {
        this.dataListener = dataListener
    }

    val date: String
        get() = if (ssQuarterlyInfo != null) {
            ssQuarterlyInfo!!.quarterly.human_date
        } else ""
    val description: String
        get() = if (ssQuarterlyInfo != null) {
            ssQuarterlyInfo!!.quarterly.description
        } else ""
    val cover: String
        get() = if (ssQuarterlyInfo != null) {
            ssQuarterlyInfo!!.quarterly.cover
        } else ""

    interface DataListener {
        fun onQuarterlyChanged(ssQuarterlyInfo: SSQuarterlyInfo)
    }

    companion object {
        @JvmStatic
        @BindingAdapter("coverUrl")
        fun loadCover(view: ImageView, coverUrl: String?) {
            ViewCompat.setElevation(view, 15.0f)
            view.load(coverUrl)
        }
    }
}
