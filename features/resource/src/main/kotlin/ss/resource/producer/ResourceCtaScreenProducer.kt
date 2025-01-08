/*
 * Copyright (c) 2025. Adventech <info@adventech.io>
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

package ss.resource.producer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.slack.circuit.runtime.screen.Screen
import io.adventech.blockkit.model.feed.FeedType
import io.adventech.blockkit.model.resource.Resource
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import org.joda.time.Interval
import ss.libraries.circuit.navigation.DocumentScreen
import ss.misc.DateHelper
import javax.inject.Inject

@Stable
interface ResourceCtaScreenProducer {

    @Composable
    operator fun invoke(resource: Resource?): Screen?
}

internal class ResourceCtaScreenProducerImpl @Inject constructor() : ResourceCtaScreenProducer {

    @Composable
    override fun invoke(resource: Resource?): Screen? {
        if (resource == null) return null
        return remember(resource) { calculateScreen(resource) }
    }

    private fun calculateScreen(
        resource: Resource,
        today: DateTime = DateTime.now()
    ): Screen? {
        val sections = resource.sections ?: return null
        val isSabbathMorning = today.dayOfWeek().get() == DateTimeConstants.SATURDAY && today.hourOfDay().get() < 12
        val dateTime = if (isSabbathMorning && resource.type == FeedType.SS) {
            today.minusDays(1)
        } else {
            today
        }

        sections.forEach { section ->
            section.documents.forEachIndexed { index, document ->
                val startDate = document.startDate?.let { DateHelper.parseDate(it) } ?: return@forEach
                val endDate = document.endDate?.let { DateHelper.parseDate(it) } ?: return@forEach

                val fallsBetween = Interval(startDate, endDate.plusDays(1)).contains(dateTime)

                if (fallsBetween) {
                    return DocumentScreen(
                        index = document.index,
                        title = document.title,
                        cover = document.cover,
                        resourceIndex = resource.index,
                        resourceId = resource.id
                    )
                }
            }
        }

        // Handle progress based documents here

        // Default to the first document and section
        return sections.firstOrNull()?.let { section ->
            section.documents.firstOrNull()?.let { document ->
                DocumentScreen(
                    index = document.index,
                    title = document.title,
                    cover = document.cover,
                    resourceIndex = resource.index,
                    resourceId = resource.id
                )
            }
        }
    }
}
