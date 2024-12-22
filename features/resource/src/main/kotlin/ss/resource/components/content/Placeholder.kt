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

import app.ss.models.resource.ResourceDocument
import app.ss.models.resource.ResourceSection
import app.ss.models.resource.Segment

internal object Placeholder {

    val resources: List<ResourceSection> = listOf(
        ResourceSection(
            id = "1",
            name = "introduction",
            title = "Introduction",
            displaySequence = true,
            isRoot = true,
            documents = listOf(
                ResourceDocument(
                    id = "1-1",
                    index = "1",
                    name = "what-is-discipleship",
                    title = "What is Discipleship?",
                    subtitle = "A comprehensive guide to understanding discipleship.",
                    resourceId = "discipleship-handbook",
                    resourceIndex = "pm",
                    sequence = "1",
                    cover = "https://example.com/cover1.jpg",
                    startDate = null,
                    endDate = null,
                    segments = null,
                    showSegmentChips = false,
                    titleBelowCover = true
                ),
                ResourceDocument(
                    id = "1-2",
                    index = "2",
                    name = "why-discipleship-matters",
                    title = "Why Discipleship Matters",
                    subtitle = "Discover the importance of discipleship in the modern world.",
                    resourceId = "discipleship-handbook",
                    resourceIndex = "pm",
                    sequence = "2",
                    cover = "https://example.com/cover2.jpg",
                    startDate = null,
                    endDate = null,
                    segments = null,
                    showSegmentChips = false,
                    titleBelowCover = true
                )
            )
        ),
        ResourceSection(
            id = "2",
            name = "practical-guidelines",
            title = "Practical Guidelines",
            displaySequence = false,
            isRoot = false,
            documents = listOf(
                ResourceDocument(
                    id = "2-1",
                    index = "1",
                    name = "how-to-disciple",
                    title = "How to Disciple",
                    subtitle = "Step-by-step guide to effective discipleship.",
                    resourceId = "discipleship-handbook",
                    resourceIndex = "pm",
                    sequence = "1",
                    cover = "https://example.com/cover3.jpg",
                    startDate = null,
                    endDate = null,
                    segments = null,
                    showSegmentChips = true,
                    titleBelowCover = false
                )
            )
        )
    )

    val resourcesWithMenu: List<ResourceSection> = listOf(
        ResourceSection(
            id = "section1",
            name = "daily-devotionals",
            title = "Daily Devotionals",
            displaySequence = true,
            isRoot = true,
            documents = listOf(
                ResourceDocument(
                    id = "doc1",
                    index = "1",
                    name = "january-1",
                    title = "January 1 - New Beginnings",
                    subtitle = "Start the year with a fresh perspective.",
                    resourceId = "bhp-2024",
                    resourceIndex = "devo",
                    sequence = "1",
                    cover = "https://example.com/covers/january-1.jpg",
                    startDate = "2024-01-01",
                    endDate = "2024-01-01",
                    segments = listOf(
                        Segment(
                            id = "seg1",
                            name = "Morning Reflection",
                            index = "Begin your day with gratitude and hope."
                        ),
                        Segment(
                            id = "seg2",
                            name = "Evening Prayer",
                            index = "Reflect on the day's blessings and challenges."
                        )
                    ),
                    showSegmentChips = true,
                    titleBelowCover = false
                ),
                ResourceDocument(
                    id = "doc2",
                    index = "2",
                    name = "january-2",
                    title = "January 2 - Embracing Change",
                    subtitle = "Adapt to the new opportunities ahead.",
                    resourceId = "bhp-2024",
                    resourceIndex = "devo",
                    sequence = "2",
                    cover = "https://example.com/covers/january-2.jpg",
                    startDate = "2024-01-02",
                    endDate = "2024-01-02",
                    segments = listOf(
                        Segment(
                            id = "seg3",
                            name = "Morning Meditation",
                            index = "Contemplate the nature of change and growth."
                        ),
                        Segment(
                            id = "seg4",
                            name = "Evening Reflection",
                            index = "Assess how you've embraced new experiences today."
                        )
                    ),
                    showSegmentChips = true,
                    titleBelowCover = false
                )
            )
        ),
        ResourceSection(
            id = "section2",
            name = "weekly-themes",
            title = "Weekly Themes",
            displaySequence = false,
            isRoot = false,
            documents = listOf(
                ResourceDocument(
                    id = "doc3",
                    index = "1",
                    name = "week-1",
                    title = "Week 1 - Faith and Trust",
                    subtitle = "Building a foundation of faith.",
                    resourceId = "bhp-2024",
                    resourceIndex = "devo",
                    sequence = "1",
                    cover = "https://example.com/covers/week-1.jpg",
                    startDate = "2024-01-01",
                    endDate = "2024-01-07",
                    segments = listOf(
                        Segment(
                            id = "seg5",
                            name = "Introduction",
                            index = "index."
                        ),
                        Segment(
                            id = "seg6",
                            name = "Daily Readings",
                            index = "index"
                        )
                    ),
                    showSegmentChips = true,
                    titleBelowCover = true
                )
            )
        )
    )
}

