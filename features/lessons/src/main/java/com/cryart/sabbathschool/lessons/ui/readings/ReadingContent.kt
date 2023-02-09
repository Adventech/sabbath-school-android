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
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.cryart.sabbathschool.lessons.ui.readings

import androidx.recyclerview.widget.DiffUtil
import app.ss.models.SSRead
import app.ss.models.SSReadComments
import app.ss.models.SSReadHighlights

/**
 * UI model for showing reading content on a single [androidx.recyclerview.widget.RecyclerView] page.
 */
internal data class ReadingContent(
    val dayIndex: Int,
    val read: SSRead,
    val highlights: SSReadHighlights,
    val comments: SSReadComments
) {
    companion object {
        val DIFF = object : DiffUtil.ItemCallback<ReadingContent>() {
            override fun areItemsTheSame(oldItem: ReadingContent, newItem: ReadingContent): Boolean {
                return oldItem.dayIndex == newItem.dayIndex
            }

            override fun areContentsTheSame(oldItem: ReadingContent, newItem: ReadingContent): Boolean {
                return oldItem == newItem
            }
        }
    }
}
