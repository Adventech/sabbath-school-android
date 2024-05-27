/*
 * Copyright (c) 2023. Adventech <info@adventech.io>
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
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package app.ss.lessons.components.spec

import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Immutable
import app.ss.lessons.components.LessonItemSpec
import app.ss.models.OfflineState
import app.ss.models.SSLesson
import app.ss.models.SSQuarterlyInfo
import app.ss.lessons.components.toSpec
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import org.joda.time.Interval
import ss.misc.DateHelper

@Immutable
internal data class QuarterlyInfoSpec(
    val title: String,
    val description: String,
    val date: String,
    val color: String,
    val colorDark: String,
    val cover: String,
    val splashImage: String?,
    val offlineState: OfflineState,
    val lessons: ImmutableList<LessonItemSpec>,
    val features: ImmutableList<FeatureSpec> = persistentListOf(),
    val todayLessonIndex: String? = null,
    val readClick: () -> Unit = {},
    val readMoreClick: () -> Unit = {},
    val offlineStateClick: () -> Unit = {}
)

internal fun SSQuarterlyInfo.toSpec(
    readMoreClick: () -> Unit = {},
    offlineStateClick: () -> Unit = {}
) = QuarterlyInfoSpec(
    title = quarterly.title,
    description = quarterly.description,
    date = quarterly.human_date,
    color = quarterly.color_primary,
    colorDark = quarterly.color_primary_dark,
    cover = quarterly.cover,
    splashImage = quarterly.splash,
    offlineState = quarterly.offlineState,
    lessons = lessons.map { it.toSpec() }.toImmutableList(),
    features = quarterly.features.map { it.toSpec() }.toImmutableList(),
    todayLessonIndex = findIndex(lessons),
    readMoreClick = readMoreClick,
    offlineStateClick = offlineStateClick
)

@VisibleForTesting
fun findIndex(
    lessons: List<SSLesson>,
    today: DateTime = DateTime.now()
): String? {
    val isSabbathMorning = today.dayOfWeek().get() == DateTimeConstants.SATURDAY && today.hourOfDay().get() < 12
    var prevLessonIndex: String? = null

    val index = lessons.find { lesson ->
        val startDate = DateHelper.parseDate(lesson.start_date)
        val endDate = DateHelper.parseDate(lesson.end_date)
        val fallsBetween = Interval(startDate, endDate?.plusDays(1)).contains(today)

        if (!fallsBetween) {
            prevLessonIndex = lesson.index
        }

        fallsBetween
    }?.index ?: lessons.firstOrNull()?.index

    return index.takeUnless { isSabbathMorning } ?: prevLessonIndex
}
