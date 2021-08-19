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

package com.cryart.sabbathschool.lessons.ui.quarterlies.components

import androidx.recyclerview.widget.DiffUtil
import app.ss.lessons.data.model.QuarterlyGroup
import app.ss.lessons.data.model.SSQuarterly

sealed interface GroupedQuarterlies {
    data class TypeList(val data: List<SSQuarterly>) : GroupedQuarterlies
    data class TypeGroup(val data: List<QuarterliesGroup>) : GroupedQuarterlies
    object Empty : GroupedQuarterlies
}

data class QuarterliesGroup(
    val group: QuarterlyGroup,
    val quarterlies: List<SSQuarterly>
)

internal val QuarterliesDiff = object : DiffUtil.ItemCallback<SSQuarterly>() {
    override fun areItemsTheSame(oldItem: SSQuarterly, newItem: SSQuarterly): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: SSQuarterly, newItem: SSQuarterly): Boolean {
        return oldItem == newItem
    }
}

internal val GroupedQuarterliesDiff = object : DiffUtil.ItemCallback<QuarterliesGroup>() {
    override fun areItemsTheSame(oldItem: QuarterliesGroup, newItem: QuarterliesGroup): Boolean {
        return oldItem.group.name == newItem.group.name
    }

    override fun areContentsTheSame(oldItem: QuarterliesGroup, newItem: QuarterliesGroup): Boolean {
        return newItem == newItem
    }
}
