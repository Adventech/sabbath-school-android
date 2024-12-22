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

package ss.resource.components.content

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import app.ss.models.feed.FeedResourceKind
import app.ss.models.resource.Resource
import app.ss.models.resource.ResourceDocument
import app.ss.models.resource.ResourceSection
import app.ss.models.resource.ResourceSectionViewType
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.CircuitUiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.joda.time.DateTime
import org.joda.time.Interval
import org.joda.time.format.DateTimeFormat
import ss.misc.DateHelper
import ss.misc.SSConstants
import javax.inject.Inject
import kotlin.collections.lastIndex

data class ResourceSectionsState(
    val specs: ImmutableList<ResourceSectionSpec>,
    val eventSink: () -> Unit
) : CircuitUiState

/** Produces [ResourceSectionsState] by building out the required [ResourceSectionSpec]s.*/
@Stable
interface ResourceSectionsStateProducer {

    @Composable
    operator fun invoke(resource: Resource): ResourceSectionsState
}

internal class ResourceSectionsStateProducerImpl @Inject constructor() : ResourceSectionsStateProducer {

    @Composable
    override fun invoke(resource: Resource): ResourceSectionsState {
        val specs = buildList<ResourceSectionSpec> {
            val content = when (resource.sectionView) {
                ResourceSectionViewType.NORMAL -> rememberNormalViewTypeSpecs(resource)
                ResourceSectionViewType.DROPDOWN -> rememberDropdownViewTypeSpecs(resource)
                else -> emptyList()
            }
            addAll(content)

        }.toImmutableList()

        return ResourceSectionsState(
            specs = specs,
        ) {

        }
    }

    @Composable
    private fun rememberNormalViewTypeSpecs(
        resource: Resource
    ): List<ResourceSectionSpec> = rememberRetained(resource) {
        buildList {
            resource.sections.orEmpty().forEachIndexed { index, section ->
                if (!section.isRoot) {
                    add(
                        HeaderResourceSection(
                            id = section.id,
                            title = section.title.uppercase()
                        )
                    )
                }
                addAll(documentsSpecs(section, resource.kind))
            }
        }
    }

    @Composable
    private fun rememberDropdownViewTypeSpecs(
        resource: Resource
    ): List<ResourceSectionSpec> {
        var selectedSection by rememberRetained(resource) { mutableStateOf(resource.defaultSection()) }

        return rememberRetained(resource, selectedSection) {
            buildList {
                add(
                    MenuResourceSection(
                        id = "popup-${resource.id}",
                        sections = resource.sections.orEmpty().toImmutableList(),
                        defaultSection = selectedSection,
                        onSelection = { selectedSection = it }
                    )
                )

                selectedSection?.let { addAll(documentsSpecs(it, resource.kind)) }
            }
        }
    }

    private fun documentsSpecs(section: ResourceSection, kind: FeedResourceKind) = buildList {
        section.documents.forEachIndexed { index, document ->
            val dateDisplay = document.dateDisplay()

            add(
                DefaultResourceSection(
                    id = document.id,
                    leadingContent = document.sequence.takeIf { section.displaySequence },
                    overlineContent = document.subtitle,
                    headLineContent = document.title,
                    supportingContent = dateDisplay,
                    isArticle = document.externalURL != null,
                    blogCover = document.cover?.takeIf { kind == FeedResourceKind.BLOG }
                )
            )

            if (index == section.documents.lastIndex) {
                add(ResourceSectionSpacer(4.dp, id = "spacer_${document.id}_$index"))
            } else {
                add(ResourceSectionDivider("divider_${document.id}_$index"))
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
                val endDate = document.endDate?.let { DateHelper.parseDate(it) } ?: return@forEach
                val fallsBetween = Interval(startDate, endDate.plusDays(1)).contains(today)
                if (fallsBetween) {
                    return section
                }
            }
        }

        return sections.orEmpty().first { it.isRoot == false }
    }
}
