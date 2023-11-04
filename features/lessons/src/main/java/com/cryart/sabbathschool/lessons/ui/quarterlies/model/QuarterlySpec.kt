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

package com.cryart.sabbathschool.lessons.ui.quarterlies.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.ss.design.compose.extensions.color.parse
import app.ss.models.OfflineState
import app.ss.models.QuarterlyGroup
import app.ss.models.SSQuarterly

@Immutable
data class QuarterlySpec(
    val id: String,
    val title: String,
    val date: String,
    val cover: String,
    val color: Color,
    val index: String,
    val offline: Boolean,
    val isPlaceholder: Boolean = false,
    val type: Type = Type.NORMAL,
    val onClick: () -> Unit = {}
) {
    enum class Type {
        NORMAL,
        LARGE;

        fun width(largeScreen: Boolean): Dp = when {
            largeScreen && this == NORMAL -> 150.dp
            largeScreen && this == LARGE -> 198.dp
            this == NORMAL -> 100.dp
            this == LARGE -> 148.dp
            else -> Dp.Unspecified
        }

        fun height(largeScreen: Boolean): Dp = when {
            largeScreen && this == NORMAL -> 198.dp
            largeScreen && this == LARGE -> 276.dp
            this == NORMAL -> 146.dp
            this == LARGE -> 226.dp
            else -> Dp.Unspecified
        }
    }
}

internal fun SSQuarterly.spec(
    type: QuarterlySpec.Type = QuarterlySpec.Type.NORMAL,
    onClick: () -> Unit = {}
): QuarterlySpec = QuarterlySpec(
    id = id,
    title = title,
    date = human_date,
    cover = cover,
    color = Color.parse(color_primary),
    index = index,
    offline = offlineState == OfflineState.COMPLETE,
    isPlaceholder = isPlaceholder,
    type = type,
    onClick = onClick,
)

internal fun QuarterlyGroup.spec() = QuarterlyGroupSpec(
    name, order
)

internal fun QuarterlyGroupSpec.group() = QuarterlyGroup(
    name, order
)

@Immutable
internal data class GroupedQuarterliesSpec(
    val title: String,
    val items: List<QuarterlySpec>,
    val lastIndex: Boolean
)
