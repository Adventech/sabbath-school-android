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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.isDigitsOnly
import app.ss.design.compose.extensions.previews.DayNightPreviews
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.widget.divider.Divider
import app.ss.models.SSLesson
import com.cryart.sabbathschool.core.misc.SSConstants
import org.joda.time.format.DateTimeFormat

@Immutable
data class LessonItemSpec(
    val index: String,
    val displayIndex: String,
    val title: String,
    val date: String,
    val pdfOnly: Boolean
)

@Immutable
internal data class LessonItemsSpec(
    val lessons: List<LessonItemSpec>
)

internal fun SSLesson.toSpec(): LessonItemSpec = LessonItemSpec(
    index = index,
    displayIndex = if (id.isDigitsOnly()) {
        "${id.toInt()}"
    } else {
        "•"
    },
    title = title,
    date = dateDisplay(),
    pdfOnly = pdfOnly
)

private fun SSLesson.dateDisplay(): String {
    val startDateOut = DateTimeFormat.forPattern(SSConstants.SS_DATE_FORMAT_OUTPUT)
        .print(
            DateTimeFormat.forPattern(SSConstants.SS_DATE_FORMAT)
                .parseLocalDate(start_date)
        )

    val endDateOut = DateTimeFormat.forPattern(SSConstants.SS_DATE_FORMAT_OUTPUT)
        .print(
            DateTimeFormat.forPattern(SSConstants.SS_DATE_FORMAT)
                .parseLocalDate(end_date)
        )

    return "$startDateOut - $endDateOut".replaceFirstChar { it.uppercase() }
}

internal fun LazyListScope.lessons(
    lessonsSpec: LessonItemsSpec,
    onClick: (LessonItemSpec) -> Unit
) {
    items(
        lessonsSpec.lessons,
        key = { it.index }
    ) { item ->
        Surface {
            LessonItem(
                spec = item,
                modifier = Modifier
                    .clickable { onClick(item) }
            )

            Divider()
        }
    }
}

@Composable
private fun LessonItem(
    spec: LessonItemSpec,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = spec.displayIndex,
            style = SsTheme.typography.titleLarge,
            color = SsTheme.colors.secondaryForeground.copy(alpha = 0.5f),
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 12.dp)
                .padding(end = 20.dp)
        ) {
            Text(
                text = spec.title,
                style = SsTheme.typography.labelMedium,
                color = SsTheme.colors.primaryForeground,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = spec.date,
                style = SsTheme.typography.bodySmall.copy(
                    fontSize = 14.sp
                ),
                color = SsTheme.colors.secondaryForeground,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }
    }
}

@DayNightPreviews
@Composable
private fun LessonItemPreview() {
    SsTheme {
        Surface {
            LessonItem(
                spec = LessonItemSpec(
                    index = "index",
                    displayIndex = "1",
                    title = "Lesson Title",
                    date = "June 25 - July 01",
                    pdfOnly = false
                )
            )
        }
    }
}
