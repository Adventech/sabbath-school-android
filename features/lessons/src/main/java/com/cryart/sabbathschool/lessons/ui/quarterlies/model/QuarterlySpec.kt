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

package com.cryart.sabbathschool.lessons.ui.quarterlies.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.ss.models.SSQuarterly
import com.cryart.design.theme.parse

@Immutable
internal data class QuarterlySpec(
    val title: String,
    val date: String,
    val cover: String,
    val color: Color,
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
    type: QuarterlySpec.Type,
    onClick: () -> Unit = {}
): QuarterlySpec = QuarterlySpec(
    title,
    human_date,
    cover,
    Color.parse(color_primary),
    isPlaceholder = isPlaceholder,
    type,
    onClick
)

@Immutable
internal data class GroupedQuarterliesSpec(
    val title: String,
    val items: List<QuarterlySpec>,
    val lastIndex: Boolean,
)
