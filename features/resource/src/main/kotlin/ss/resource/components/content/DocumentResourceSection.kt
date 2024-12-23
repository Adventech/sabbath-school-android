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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ss.design.compose.extensions.modifier.asPlaceholder
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.widget.content.ContentBox
import app.ss.design.compose.widget.icon.IconBox
import app.ss.design.compose.widget.icon.Icons
import app.ss.design.compose.widget.image.RemoteImage
import app.ss.models.feed.FeedResourceKind
import app.ss.models.resource.ResourceDocument
import org.joda.time.format.DateTimeFormat
import ss.misc.SSConstants

@Immutable
data class DocumentResourceSection(
    private val document: ResourceDocument,
    override val id: String = document.id,
    private val displaySequence: Boolean,
    private val resourceKind: FeedResourceKind,
    private val onClick: () -> Unit
) : ResourceSectionSpec {

    @Composable
    override fun Content(modifier: Modifier) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .sizeIn(minHeight = 48.dp)
                .padding(horizontal = 6.dp, vertical = 2.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable { onClick() }
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            document.sequence.takeIf { displaySequence }?.let {
                Text(
                    text = it,
                    modifier = Modifier.padding(end = 12.dp),
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

            if (document.externalURL != null) {
                IconBox(Icons.OpenInBrowser)
            }

            document.cover?.takeIf { resourceKind == FeedResourceKind.BLOG }?.let {
                ContentBox(
                    content = RemoteImage(
                        data = it,
                        loading = {
                            Spacer(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .asPlaceholder(true, shape = CoverImageShape)
                            )
                        },
                        error = {
                            Spacer(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.secondaryContainer, CoverImageShape)
                            )
                        }
                    ),
                    modifier = Modifier
                        .width(80.dp)
                        .aspectRatio(16f / 9f)
                        .background(MaterialTheme.colorScheme.secondaryContainer, CoverImageShape)
                        .shadow(4.dp, CoverImageShape)
                )
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
}

private val CoverImageShape = RoundedCornerShape(6.dp)

@PreviewLightDark
@Composable
private fun Preview() {
    SsTheme {
        Surface {
            DocumentResourceSection(
                document = Placeholder.document,
                displaySequence = true,
                resourceKind = FeedResourceKind.PLAN,
                onClick = {}
            ).Content()
        }
    }
}
