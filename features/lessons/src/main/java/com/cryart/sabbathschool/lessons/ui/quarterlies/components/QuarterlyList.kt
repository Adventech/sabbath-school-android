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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import app.ss.design.compose.extensions.modifier.thenIf
import app.ss.design.compose.extensions.scrollbar.drawVerticalScrollbar
import app.ss.models.QuarterlyGroup
import com.cryart.sabbathschool.lessons.ui.quarterlies.model.GroupedQuarterlies
import com.cryart.sabbathschool.lessons.ui.quarterlies.model.GroupedQuarterliesSpec
import com.cryart.sabbathschool.lessons.ui.quarterlies.model.group

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
internal fun QuarterlyList(
    quarterlies: GroupedQuarterlies,
    modifier: Modifier = Modifier,
    callbacks: QuarterlyListCallbacks? = null,
    state: LazyListState = rememberLazyListState()
) {
    LazyColumn(
        modifier = modifier
            .drawVerticalScrollbar(state)
            .testTag("quarterlies:list"),
        state = state
    ) {
        when (quarterlies) {
            GroupedQuarterlies.Empty -> { /* No op */
            }
            is GroupedQuarterlies.TypeGroup -> {
                itemsIndexed(
                    quarterlies.data,
                    key = { _, model -> model.group.name },
                    itemContent = { index, model ->
                        val items = remember(model.group.name) {
                            model.quarterlies.map { quarterly ->
                                quarterly.copy(
                                    onClick = {
                                        callbacks?.onReadClick(quarterly.index)
                                    }
                                )
                            }
                        }

                        GroupedQuarterliesColumn(
                            spec = GroupedQuarterliesSpec(
                                title = model.group.name,
                                items = items,
                                index == quarterlies.data.lastIndex
                            ),
                            modifier = Modifier.testTag("quarterlies:group")
                        ) {
                            (callbacks as? QuarterliesGroupCallback)?.onSeeAllClick(model.group.group())
                        }
                    }
                )
            }
            is GroupedQuarterlies.TypeList -> {
                items(
                    quarterlies.data,
                    key = { it.id }
                ) { spec ->
                    QuarterlyRow(
                        spec = spec,
                        modifier = Modifier
                            .thenIf(spec.isPlaceholder.not()) {
                                clickable {
                                    callbacks?.onReadClick(spec.index)
                                }
                            }
                    )
                }
            }
        }

        item {
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
        }
    }
}
