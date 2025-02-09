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
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuitx.android.IntentScreen
import io.adventech.blockkit.model.feed.FeedResourceKind
import io.adventech.blockkit.model.resource.Resource
import io.adventech.blockkit.model.resource.ResourceDocument
import io.adventech.blockkit.model.resource.ResourceSection
import io.adventech.blockkit.model.resource.ResourceSectionViewType
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.joda.time.DateTime
import org.joda.time.Interval
import ss.libraries.circuit.navigation.CustomTabsIntentScreen
import ss.libraries.circuit.navigation.DocumentScreen
import ss.libraries.pdf.api.PdfReader
import ss.misc.DateHelper
import javax.inject.Inject
import kotlin.collections.lastIndex

data class ResourceSectionsState(
    val specs: ImmutableList<ResourceSectionSpec>,
) : CircuitUiState

/** Produces [ResourceSectionsState] by building out the required [ResourceSectionSpec]s.*/
@Stable
interface ResourceSectionsStateProducer {

    @Composable
    operator fun invoke(navigator: Navigator, resource: Resource): ResourceSectionsState
}

internal class ResourceSectionsStateProducerImpl @Inject constructor(
    private val pdfReader: PdfReader
) : ResourceSectionsStateProducer {

    @Composable
    override fun invoke(navigator: Navigator, resource: Resource): ResourceSectionsState {
        val onDocumentClick: (ResourceDocument) -> Unit = { document ->
            when {
                !document.externalURL.isNullOrEmpty() -> document.externalURL?.let {
                    navigator.goTo(CustomTabsIntentScreen(it))
                }
                else -> {
                    val screen = document.pdfScreen()?.let {
                        IntentScreen(pdfReader.launchIntent(it))
                    } ?: DocumentScreen(
                        id = document.id,
                        index = document.index,
                        cover = document.cover,
                    )
                    navigator.goTo(screen)
                }
            }
        }

        val specs = buildList<ResourceSectionSpec> {
            val content = when (resource.sectionView) {
                ResourceSectionViewType.NORMAL -> rememberNormalViewTypeSpecs(resource, onDocumentClick)
                ResourceSectionViewType.DROPDOWN -> rememberDropdownViewTypeSpecs(resource, onDocumentClick)
                else -> emptyList()
            }
            addAll(content)

        }.toImmutableList()

        return ResourceSectionsState(specs)
    }

    @Composable
    private fun rememberNormalViewTypeSpecs(
        resource: Resource,
        onDocumentClick: (ResourceDocument) -> Unit,
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
                addAll(documentsSpecs(section, resource.kind, onDocumentClick))
            }
        }
    }

    @Composable
    private fun rememberDropdownViewTypeSpecs(
        resource: Resource,
        onDocumentClick: (ResourceDocument) -> Unit,
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

                selectedSection?.let { addAll(documentsSpecs(it, resource.kind, onDocumentClick)) }
            }
        }
    }

    private fun documentsSpecs(
        section: ResourceSection,
        kind: FeedResourceKind,
        onDocumentClick: (ResourceDocument) -> Unit
    ) = buildList {
        section.documents.forEachIndexed { index, document ->
            add(
                DocumentResourceSection(
                    document = document,
                    displaySequence = section.displaySequence,
                    resourceKind = kind,
                    onClick = { onDocumentClick(document) }
                )
            )

            if (index == section.documents.lastIndex) {
                add(ResourceSectionSpacer(4.dp, id = "spacer_${document.id}_$index"))
            } else {
                add(ResourceSectionDivider("divider_${document.id}_$index"))
            }
        }
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
