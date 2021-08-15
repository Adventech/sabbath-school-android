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

import androidx.compose.ui.platform.ComposeView
import app.ss.lessons.data.model.QuarterlyGroup
import app.ss.lessons.data.model.SSQuarterly
import com.cryart.design.theme.SSTheme
import kotlinx.coroutines.flow.Flow

sealed interface GroupedQuarterlies {
    data class TypeList(val data: List<SSQuarterly>) : GroupedQuarterlies
    data class TypeMap(val data: Map<QuarterlyGroup, List<SSQuarterly>>) : GroupedQuarterlies
    object Empty : GroupedQuarterlies
}

class QuarterliesListComponent(
    private val dataFlow: Flow<GroupedQuarterlies>,
    private val composeView: ComposeView
) {
    init {

        /*when (data) {
            is GroupedQuarterlies.TypeList -> Timber.d("DATA IS LIST: ${data.data.size}")
            is GroupedQuarterlies.TypeMap -> Timber.d("DATA IS MAP: ${data.data.keys.size}")
            GroupedQuarterlies.Empty -> Timber.d("DATA IS EMPTY:")
        }*/

        composeView.setContent {
            SSTheme {
            }
        }
    }
}
