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

package com.cryart.sabbathschool.lessons.ui.lessons.components

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import androidx.core.text.parseAsHtml
import androidx.lifecycle.LifecycleOwner
import app.ss.lessons.data.model.SSQuarterlyInfo
import com.cryart.sabbathschool.core.extensions.coroutines.flow.collectIn
import com.cryart.sabbathschool.core.misc.DateHelper
import com.cryart.sabbathschool.core.misc.SSColorTheme
import com.cryart.sabbathschool.core.ui.BaseComponent
import com.cryart.sabbathschool.lessons.databinding.SsLessonDescriptionBinding
import com.cryart.sabbathschool.lessons.databinding.SsLessonsQuarterlyInfoBinding
import com.cryart.sabbathschool.lessons.ui.base.loadCover
import com.cryart.sabbathschool.lessons.ui.readings.SSReadingActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.flow.Flow
import org.joda.time.DateTime
import org.joda.time.Interval

class QuarterlyInfoComponent(
    lifecycleOwner: LifecycleOwner,
    private val binding: SsLessonsQuarterlyInfoBinding
) : BaseComponent<SSQuarterlyInfo?>(lifecycleOwner) {

    private var todayLessonIndex: String? = null

    init {
        binding.ssLessonsAppBarRead.setOnClickListener { view ->
            todayLessonIndex?.let { index ->
                val context = view.context
                val ssReadingIntent = SSReadingActivity.launchIntent(context, index)
                context.startActivity(ssReadingIntent)
            }
        }
    }

    override fun collect(visibilityFlow: Flow<Boolean>, dataFlow: Flow<SSQuarterlyInfo?>) {
        dataFlow.collectIn(owner) { data ->
            data?.let { setQuarterlyInfo(it) }
        }
    }

    @SuppressLint("Range")
    private fun setQuarterlyInfo(quarterlyInfo: SSQuarterlyInfo) {
        val primaryColor = Color.parseColor(quarterlyInfo.quarterly.color_primary)
        val primaryDarkColor = Color.parseColor(quarterlyInfo.quarterly.color_primary_dark)

        with(SSColorTheme.getInstance(binding.root.context)) {
            colorPrimary = quarterlyInfo.quarterly.color_primary
            colorPrimaryDark = quarterlyInfo.quarterly.color_primary_dark
        }

        binding.apply {
            appBarContent.setBackgroundColor(primaryColor)
            ssLessonsAppBarCover.loadCover(quarterlyInfo.quarterly.cover, primaryDarkColor)
            ssLessonsAppBarTitle.text = quarterlyInfo.quarterly.title
            ssLessonsAppBarDate.text = quarterlyInfo.quarterly.human_date
            ssLessonsAppBarDescription.text = quarterlyInfo.quarterly.description
            ssLessonsAppBarDescription.setOnClickListener {
                showDescription(quarterlyInfo.quarterly.title, quarterlyInfo.quarterly.description)
            }
            ssLessonsAppBarRead.backgroundTintList = ColorStateList.valueOf(primaryDarkColor)
        }

        val today = DateTime.now().withTimeAtStartOfDay()
        todayLessonIndex = quarterlyInfo.lessons.find { lesson ->
            val startDate = DateHelper.parseDate(lesson.start_date)
            val endDate = DateHelper.parseDate(lesson.end_date)
            Interval(startDate, endDate?.plusDays(1)).contains(today)
        }?.index ?: quarterlyInfo.lessons.firstOrNull()?.index
    }

    private fun showDescription(title: String, description: String) {
        val context = binding.root.context
        val binding = SsLessonDescriptionBinding.inflate(LayoutInflater.from(context))
        val dialog = BottomSheetDialog(context)

        binding.txtContent.text = description.parseAsHtml()
        binding.toolbar.title = title
        binding.toolbar.setNavigationOnClickListener {
            dialog.dismiss()
        }
        dialog.setContentView(binding.root)
        dialog.show()
    }
}
