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

package app.ss.quarterlies.model

import androidx.compose.runtime.Immutable
import app.ss.models.SSQuarterly
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

sealed interface GroupedQuarterlies {
    @Immutable
    data class TypeList(val data: ImmutableList<QuarterlySpec>) : GroupedQuarterlies

    @Immutable
    data class TypeGroup(val data: ImmutableList<QuarterliesGroupModel>) : GroupedQuarterlies

    @Immutable
    data object Empty : GroupedQuarterlies
}

@Immutable
data class QuarterliesGroupModel(
    val group: QuarterlyGroupSpec,
    val quarterlies: List<QuarterlySpec>
)

@Immutable
data class QuarterlyGroupSpec(
    val name: String,
    val order: Int
)

private const val PLACEHOLDER_ID = "PLACEHOLDER_QUARTERLY"

internal val SSQuarterly.isPlaceholder: Boolean get() = id.startsWith(PLACEHOLDER_ID)

internal fun placeHolderQuarterlies(): ImmutableList<QuarterlySpec> {
    val placeHolders = mutableListOf<SSQuarterly>()
    for (i in 0 until 10) {
        placeHolders.add(
            SSQuarterly(
                "${PLACEHOLDER_ID}_$i",
                title = "Quarterly placeholder",
                human_date = "Some * date * here",
                color_primary = "#2E5797"
            )
        )
    }
    return placeHolders.map { it.spec() }.toImmutableList()
}
