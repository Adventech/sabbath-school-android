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
import android.view.View
import android.view.ViewStub
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import app.ss.models.Feature
import app.ss.models.SSQuarterlyInfo
import com.cryart.design.color.withAlpha
import com.cryart.sabbathschool.core.extensions.coroutines.flow.collectIn
import com.cryart.sabbathschool.core.extensions.view.addMoreEllipses
import com.cryart.sabbathschool.core.extensions.view.fadeTo
import com.cryart.sabbathschool.core.misc.DateHelper
import com.cryart.sabbathschool.core.ui.BaseComponent
import com.cryart.sabbathschool.lessons.R
import com.cryart.sabbathschool.lessons.databinding.SsLessonsQuarterlyInfoBinding
import com.cryart.sabbathschool.lessons.ui.base.loadCover
import com.cryart.sabbathschool.lessons.ui.lessons.components.features.QuarterlyFeaturesRow
import com.cryart.sabbathschool.lessons.ui.lessons.intro.LessonIntroModel
import com.cryart.sabbathschool.lessons.ui.lessons.intro.showLessonIntro
import com.cryart.sabbathschool.lessons.ui.readings.SSReadingActivity
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.flow.Flow
import org.joda.time.DateTime
import org.joda.time.Interval

internal class QuarterlyInfoComponent(
    lifecycleOwner: LifecycleOwner,
    private val fragmentManager: FragmentManager,
    private val binding: SsLessonsQuarterlyInfoBinding,
    private val lessonsCallback: LessonsCallback,
) : BaseComponent<SSQuarterlyInfo?>(lifecycleOwner) {

    override fun collect(visibilityFlow: Flow<Boolean>, dataFlow: Flow<SSQuarterlyInfo?>) {
        visibilityFlow.collectIn(owner) { visible ->
            binding.root.fadeTo(visible)
        }

        dataFlow.collectIn(owner) { data ->
            data?.let { quarterlyInfo ->
                setQuarterlyInfo(quarterlyInfo)

                val today = DateTime.now().withTimeAtStartOfDay()
                val todayLessonIndex = quarterlyInfo.lessons.find { lesson ->
                    val startDate = DateHelper.parseDate(lesson.start_date)
                    val endDate = DateHelper.parseDate(lesson.end_date)
                    Interval(startDate, endDate?.plusDays(1)).contains(today)
                }?.index ?: quarterlyInfo.lessons.firstOrNull()?.index

                binding.root.findViewById<View?>(R.id.ss_lessons_app_bar_read)
                    ?.setOnClickListener { view ->
                        todayLessonIndex?.let index@{ index ->
                            val lesson = quarterlyInfo.lessons.firstOrNull { it.index == index } ?: return@index
                            if (lesson.pdfOnly) {
                                lessonsCallback.openPdf(lesson)
                            } else {
                                val context = view.context
                                val ssReadingIntent = SSReadingActivity.launchIntent(context, index)
                                context.startActivity(ssReadingIntent)
                            }
                        }
                    }
            }
        }
    }

    @SuppressLint("Range")
    private fun setQuarterlyInfo(quarterlyInfo: SSQuarterlyInfo) {
        val quarterly = quarterlyInfo.quarterly
        val introModel = LessonIntroModel(
            quarterly.title,
            quarterly.introduction ?: quarterly.description
        )
        val primaryColor = Color.parseColor(quarterly.color_primary)
        val primaryDarkColor = Color.parseColor(quarterly.color_primary_dark)

        val viewStub: ViewStub? = binding.root.findViewById(R.id.viewStub)
        viewStub?.layoutResource = quarterly.splash?.let {
            R.layout.ss_lessons_quarterly_info_splash_stub
        } ?: R.layout.ss_lessons_quarterly_info_stub
        viewStub?.inflate()

        binding.root.apply {
            quarterly.splash?.let { url ->
                findViewById<ImageView?>(R.id.ss_quarterly_splash)
                    ?.loadCover(url, primaryColor)

                val array = arrayListOf(
                    primaryDarkColor,
                    primaryColor,
                    Color.BLACK.withAlpha(40),
                    Color.TRANSPARENT
                )
                val background = GradientDrawable(
                    GradientDrawable.Orientation.BOTTOM_TOP,
                    array.toIntArray()
                )
                findViewById<View?>(R.id.ss_quarterly_splash_gradient)?.background = background
            } ?: run {
                setBackgroundColor(primaryColor)
                findViewById<ImageView?>(R.id.ss_lessons_app_bar_cover)
                    ?.loadCover(quarterly.cover, primaryDarkColor)
            }

            findViewById<TextView>(R.id.ss_lessons_app_bar_title)
                ?.text = quarterly.title

            findViewById<TextView>(R.id.ss_lessons_app_bar_date)
                ?.text = quarterly.human_date

            findViewById<TextView>(R.id.ss_lessons_app_bar_description)?.apply {
                text = quarterly.description
                addMoreEllipses(
                    3,
                    R.string.ss_more,
                    R.color.text_link
                )
                setOnClickListener {
                    fragmentManager.showLessonIntro(introModel)
                }
            }

            findViewById<MaterialButton>(R.id.ss_lessons_app_bar_read)
                ?.backgroundTintList = ColorStateList.valueOf(primaryDarkColor)

            findViewById<ComposeView?>(R.id.quarterly_features_view)
                ?.let { view -> showFeatures(view, quarterly.features) }
        }
    }

    private fun showFeatures(view: ComposeView, features: List<Feature>) {
        view.setContent {
            QuarterlyFeaturesRow(features = features)
        }
        view.isVisible = features.isNotEmpty()
    }

    fun onContentScroll(scrollY: Int) {
        binding.root.findViewById<View?>(R.id.ss_quarterly_splash)?.translationY = scrollY * 0.5f
    }
}
