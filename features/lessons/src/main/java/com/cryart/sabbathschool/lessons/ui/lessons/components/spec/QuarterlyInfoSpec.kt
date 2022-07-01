/*
 * Copyright (c) 2022. Adventech <info@adventech.io>
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

package com.cryart.sabbathschool.lessons.ui.lessons.components.spec

import androidx.compose.runtime.Immutable
import app.ss.models.SSQuarterlyInfo
import com.cryart.sabbathschool.core.misc.DateHelper
import org.joda.time.DateTime
import org.joda.time.Interval

@Immutable
data class QuarterlyInfoSpec(
    val title: String,
    val description: String,
    val date: String,
    val color: String,
    val colorDark: String,
    val cover: String,
    val splashImage: String?,
    val features: List<FeatureSpec> = emptyList(),
    val todayLessonIndex: String? = null,
    val readClick: () -> Unit = {},
    val readMoreClick: () -> Unit = {},
)

internal fun SSQuarterlyInfo.toSpec() = QuarterlyInfoSpec(
    title = quarterly.title,
    description = quarterly.description,
    date = quarterly.human_date,
    color = quarterly.color_primary,
    colorDark = quarterly.color_primary_dark,
    cover = quarterly.cover,
    splashImage = quarterly.splash,
    features = quarterly.features.map { it.toSpec() },
    todayLessonIndex = lessons.find { lesson ->
        val startDate = DateHelper.parseDate(lesson.start_date)
        val endDate = DateHelper.parseDate(lesson.end_date)
        Interval(startDate, endDate?.plusDays(1)).contains(DateTime.now().withTimeAtStartOfDay())
    }?.index ?: lessons.firstOrNull()?.index
)
