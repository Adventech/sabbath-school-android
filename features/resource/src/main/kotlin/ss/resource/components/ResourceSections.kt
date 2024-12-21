/*
 * Copyright (c) 2024. Adventech <info@adventech.io>
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

package ss.resource.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.widget.divider.Divider
import app.ss.design.compose.widget.icon.IconBox
import app.ss.design.compose.widget.icon.Icons
import app.ss.models.resource.Resource
import app.ss.models.resource.ResourceDocument
import app.ss.models.resource.ResourceSection
import app.ss.models.resource.ResourceSectionViewType
import org.joda.time.DateTime
import org.joda.time.Interval
import org.joda.time.format.DateTimeFormat
import ss.misc.DateHelper
import ss.misc.SSConstants

internal fun LazyListScope.resourceSections(resource: Resource) {
    item {
        Surface {
            Spacer(
                Modifier
                    .fillMaxWidth()
                    .height(16.dp)
            )
        }
    }

    when (resource.sectionView) {
        ResourceSectionViewType.NORMAL -> {
            items(resource.sections.orEmpty(), key = { it.id }) { section ->
                Surface {
                    ResourceSectionView(section = section, displaySectionName = true)
                }
            }
        }

        ResourceSectionViewType.DROPDOWN -> {
            val sections = resource.sections.orEmpty()

            item("popup") {
                var expanded by remember { mutableStateOf(false) }
                var selectedSection by remember { mutableStateOf<ResourceSection?>(resource.defaultSection()) }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    shape = RoundedCornerShape(16.dp),
                ) {
                    sections.forEach { section ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = section.title,
                                    modifier = Modifier,
                                    style = SsTheme.typography.titleMedium
                                )
                            },
                            onClick = {
                                expanded = false
                                selectedSection = section
                            },
                            modifier = Modifier,
                            trailingIcon = {
                                if (section == selectedSection) {
                                    IconBox(Icons.Check)
                                }
                            }
                        )
                    }
                }

                Surface {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 6.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { expanded = true }
                                .padding(horizontal = 12.dp)
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = selectedSection?.title ?: "",
                                modifier = Modifier,
                                style = SsTheme.typography.titleMedium.copy(
                                    fontSize = 18.sp
                                ),
                                maxLines = 1
                            )

                            IconBox(Icons.ArrowDropDown)
                        }

                        selectedSection?.let {
                            ResourceSectionView(section = it, displaySectionName = false)
                        }
                    }
                }
            }
        }

        else -> Unit
    }
}

@Composable
private fun ResourceSectionView(
    section: ResourceSection,
    modifier: Modifier = Modifier,
    displaySectionName: Boolean = true,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (!section.isRoot && displaySectionName) {
            Text(
                text = section.title.uppercase(),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SsTheme.colors.primaryBackground.copy(alpha = 0.03f))
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                style = SsTheme.typography.titleSmall.copy(
                    fontSize = 13.sp
                ),
                color = SsTheme.colors.primaryForeground.copy(alpha = 0.4f)
            )

            Divider(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
        }

        section.documents.forEachIndexed { index, document ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {}
                    .padding(horizontal = 12.dp)
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (section.displaySequence) {
                    Text(
                        text = document.sequence,
                        modifier = Modifier.padding(end = if (document.sequence.length == 1) 16.dp else 12.dp),
                        style = SsTheme.typography.titleSmall.copy(fontSize = 22.sp),
                        color = SsTheme.colors.primaryForeground,
                        maxLines = 1,
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    document.subtitle?.let {
                        Text(
                            text = it,
                            modifier = Modifier,
                            style = SsTheme.typography.bodyMedium.copy(fontSize = 16.sp),
                            color = SsTheme.colors.secondaryForeground,
                            maxLines = 1,
                        )
                    }

                    Text(
                        text = document.title,
                        modifier = Modifier,
                        style = SsTheme.typography.titleMedium.copy(fontSize = 18.sp),
                        color = SsTheme.colors.primaryForeground,
                        maxLines = 2,
                    )

                    val dateDisplay = remember(document) { document.dateDisplay() }
                    dateDisplay?.let {
                        Text(
                            text = it,
                            modifier = Modifier,
                            style = SsTheme.typography.bodyMedium.copy(fontSize = 16.sp),
                            color = SsTheme.colors.secondaryForeground,
                            maxLines = 1,
                        )
                    }
                }
            }

            if (index == section.documents.lastIndex) {
                Spacer(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
            } else {
                Divider(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
            }
        }
    }

}

private fun ResourceDocument.dateDisplay(): String? {
    val dateStart = startDate?.let {
        DateTimeFormat.forPattern(SSConstants.SS_DATE_FORMAT)
            .parseLocalDate(it)
    } ?: return null
    val dateEnd = endDate?.let {
        DateTimeFormat.forPattern(SSConstants.SS_DATE_FORMAT)
            .parseLocalDate(it)
    } ?: return null

    if (dateStart.isEqual(dateEnd)) {
        return DateTimeFormat.forPattern(SSConstants.SS_DATE_FORMAT_OUTPUT_DAY)
            .print(dateStart)
    }

    val startDateOut = DateTimeFormat.forPattern(SSConstants.SS_DATE_FORMAT_LESSON_OUTPUT)
        .print(dateStart)

    val endDateOut = DateTimeFormat.forPattern(SSConstants.SS_DATE_FORMAT_LESSON_OUTPUT)
        .print(dateEnd)

    return "$startDateOut - $endDateOut".replaceFirstChar { it.uppercase() }
}

private fun Resource.defaultSection(today: DateTime = DateTime.now()): ResourceSection? {
    sections.orEmpty().forEach { section ->
        section.documents.forEach { document ->
            val startDate = document.startDate?.let { DateHelper.parseDate(it) } ?: return@forEach
            val endDate = document.endDate?.let {  DateHelper.parseDate(it) } ?: return@forEach
            val fallsBetween = Interval(startDate, endDate.plusDays(1)).contains(today)
            if (fallsBetween) {
                return section
            }
        }
    }

    return sections.orEmpty().first { it.isRoot == false }
}

@PreviewLightDark
@Composable
private fun Preview() {
    SsTheme {
        Surface {
            ResourceSectionView(
                section = ResourceSection(
                    id = "",
                    name = "Name",
                    title = "Title",
                    displaySequence = true,
                    documents = listOf(
                        ResourceDocument(
                            id = "1",
                            index = "index",
                            name = "01",
                            title = "Signs That Point the Way",
                            subtitle = null,
                            resourceId = "en-ss-2024-04",
                            resourceIndex = "en/ss/2024-04",
                            sequence = "1",
                            cover = "cover",
                            startDate = "28/09/2024",
                            endDate = "04/10/2024",
                            segments = emptyList(),
                            showSegmentChips = false,
                            titleBelowCover = false,
                        ),
                    ),
                    isRoot = true
                ),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun PreviewNoSequence() {
    SsTheme {
        Surface {
            ResourceSectionView(
                section = ResourceSection(
                    id = "en-pm-discipleship-handbook-root",
                    name = "root",
                    title = "Main",
                    displaySequence = false,
                    documents = listOf(
                        ResourceDocument(
                            id = "en-pm-discipleship-handbook-00-introduction",
                            index = "index",
                            name = "00-introduction",
                            title = "Introduction",
                            subtitle = null,
                            resourceId = "en-ss-2024-04",
                            resourceIndex = "en/ss/2024-04",
                            sequence = "â€¢",
                            cover = "cover",
                            startDate = null,
                            endDate = null,
                            segments = emptyList(),
                            showSegmentChips = null,
                            titleBelowCover = null,
                        ),
                    ),
                    isRoot = true
                ),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun PreviewNoSequenceNotRoot() {
    SsTheme {
        Surface {
            ResourceSectionView(
                section = ResourceSection(
                    id = "en-pm-discipleship-handbook-01-discipleship",
                    name = "01-discipleship",
                    title = "Discipleship",
                    displaySequence = false,
                    documents = listOf(
                        ResourceDocument(
                            id = "en-pm-discipleship-handbook-00-introduction",
                            index = "index",
                            name = "00-introduction",
                            title = "To Be Like Jesus",
                            subtitle = "Chapter 1",
                            resourceId = "en-ss-2024-04",
                            resourceIndex = "en/ss/2024-04",
                            sequence = "â€¢",
                            cover = "cover",
                            startDate = null,
                            endDate = null,
                            segments = emptyList(),
                            showSegmentChips = null,
                            titleBelowCover = null,
                        ),
                    ),
                    isRoot = false
                ),
            )
        }
    }
}
