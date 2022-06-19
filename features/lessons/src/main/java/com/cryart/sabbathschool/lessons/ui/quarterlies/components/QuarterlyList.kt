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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.ss.design.compose.extensions.modifier.thenIf
import app.ss.design.compose.extensions.scrollbar.drawVerticalScrollbar
import app.ss.models.QuarterlyGroup
import app.ss.models.SSQuarterly
import com.cryart.sabbathschool.lessons.ui.quarterlies.model.GroupedQuarterlies
import com.cryart.sabbathschool.lessons.ui.quarterlies.model.GroupedQuarterliesSpec
import com.cryart.sabbathschool.lessons.ui.quarterlies.model.QuarterliesGroup
import com.cryart.sabbathschool.lessons.ui.quarterlies.model.QuarterlySpec
import com.cryart.sabbathschool.lessons.ui.quarterlies.model.isPlaceholder
import com.cryart.sabbathschool.lessons.ui.quarterlies.model.spec

interface QuarterlyListCallbacks {
    fun onReadClick(index: String)
}

interface QuarterliesGroupCallback : QuarterlyListCallbacks {
    fun onSeeAllClick(group: QuarterlyGroup)
    fun profileClick()
    fun filterLanguages()
}

interface QuarterliesListCallback : QuarterlyListCallbacks {
    fun backNavClick()
}

@Composable
fun QuarterlyList(
    data: GroupedQuarterlies,
    modifier: Modifier = Modifier,
    callbacks: QuarterlyListCallbacks? = null,
    state: LazyListState = rememberLazyListState()
) {

    LazyColumn(
        modifier = modifier
            .drawVerticalScrollbar(state),
        state = state
    ) {
        when (data) {
            GroupedQuarterlies.Empty -> { /* No op */ }
            is GroupedQuarterlies.TypeGroup -> {
                val groupData = data.data
                itemsIndexed(
                    groupData,
                    key = { _: Int, item: QuarterliesGroup -> item.group.name }
                ) { index, group ->

                    val items = group.quarterlies.map { quarterly ->
                        quarterly.spec(
                            QuarterlySpec.Type.LARGE,
                            onClick = {
                                callbacks?.onReadClick(quarterly.index)
                            }
                        )
                    }

                    GroupedQuarterliesColumn(
                        spec = GroupedQuarterliesSpec(
                            title = group.group.name,
                            items = items,
                            index == groupData.lastIndex
                        )
                    ) {
                        (callbacks as? QuarterliesGroupCallback)?.onSeeAllClick(group.group)
                    }
                }
            }
            is GroupedQuarterlies.TypeList -> {
                itemsIndexed(
                    data.data,
                    key = { _: Int, item: SSQuarterly -> item.id }
                ) { _, item ->
                    QuarterlyRow(
                        spec = item.spec(QuarterlySpec.Type.NORMAL),
                        modifier = Modifier
                            .thenIf(item.isPlaceholder.not()) {
                                clickable {
                                    callbacks?.onReadClick(item.index)
                                }
                            },
                    )
                }
            }
        }
    }
}
