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
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import androidx.core.text.parseAsHtml
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import app.ss.lessons.data.model.SSQuarterlyInfo
import com.cryart.design.color.withAlpha
import com.cryart.sabbathschool.core.extensions.coroutines.flow.collectIn
import com.cryart.sabbathschool.core.extensions.view.fadeTo
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
        visibilityFlow.collectIn(owner) { visible ->
            binding.root.fadeTo(visible)
        }

        dataFlow.collectIn(owner) { data ->
            data?.let { setQuarterlyInfo(it) }
        }
    }

    @SuppressLint("Range")
    private fun setQuarterlyInfo(quarterlyInfo: SSQuarterlyInfo) {
        val quarterly = quarterlyInfo.quarterly
        val primaryColor = Color.parseColor(quarterly.color_primary)
        val primaryDarkColor = Color.parseColor(quarterly.color_primary_dark)

        with(SSColorTheme.getInstance(binding.root.context)) {
            colorPrimary = quarterlyInfo.quarterly.color_primary
            colorPrimaryDark = quarterlyInfo.quarterly.color_primary_dark
        }

        binding.apply {
            quarterly.splash?.let { splash ->
                ssQuarterlyItemCoverCard.isInvisible = true
                ssQuarterlySplash.isVisible = true
                ssQuarterlySplash.loadCover(splash, primaryColor)

                val background = GradientDrawable(
                    GradientDrawable.Orientation.BOTTOM_TOP,
                    intArrayOf(
                        primaryDarkColor,
                        primaryColor,
                        Color.WHITE.withAlpha(20),
                        Color.TRANSPARENT
                    )
                )
                ssQuarterlySplashGradient.background = background
            } ?: run {
                appBarContent.setBackgroundColor(primaryColor)
                ssQuarterlySplashGradient.isVisible = false
                ssQuarterlySplash.isVisible = false
                ssQuarterlyItemCoverCard.isVisible = true
                ssLessonsAppBarCover.loadCover(quarterly.cover, primaryDarkColor)
            }
            ssLessonsAppBarTitle.text = quarterly.title
            ssLessonsAppBarDate.text = quarterly.human_date
            ssLessonsAppBarDescription.text = quarterly.description
            ssLessonsAppBarDescription.setOnClickListener {
                showDescription(quarterly.title, quarterly.description)
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
