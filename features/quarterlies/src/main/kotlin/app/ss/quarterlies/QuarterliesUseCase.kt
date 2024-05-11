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

package app.ss.quarterlies

import app.ss.models.QuarterlyGroup
import app.ss.models.SSQuarterly
import app.ss.quarterlies.model.GroupedQuarterlies
import app.ss.quarterlies.model.QuarterliesGroupModel
import app.ss.quarterlies.model.QuarterlySpec
import app.ss.quarterlies.model.spec
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject
import javax.inject.Singleton

fun interface QuarterliesUseCase {
    fun group(result: Result<List<SSQuarterly>>): GroupedQuarterlies
}

@Singleton
class QuarterliesUseCaseImpl @Inject constructor() : QuarterliesUseCase {
    override fun group(result: Result<List<SSQuarterly>>): GroupedQuarterlies {
        val data = result.getOrNull() ?: run {
            return GroupedQuarterlies.Empty
        }
        val grouped = data
            .groupBy { it.quarterly_group }
            .toSortedMap(compareBy { it?.order })

        val groupType = when (grouped.keys.size) {
            0 -> GroupedQuarterlies.Empty
            1 -> {
                val specs = grouped[grouped.firstKey()]?.map { it.spec() }?.toImmutableList()
                GroupedQuarterlies.TypeList(specs ?: persistentListOf())
            }

            else -> {
                @Suppress("UNCHECKED_CAST")
                val filtered = grouped.filterKeys { it != null } as Map<QuarterlyGroup, List<SSQuarterly>>
                if (filtered.keys.size > 1) {
                    val groups = filtered.map { map ->
                        QuarterliesGroupModel(
                            group = map.key.spec(),
                            quarterlies = map.value.map { it.spec(QuarterlySpec.Type.LARGE) }
                        )
                    }.toImmutableList()
                    GroupedQuarterlies.TypeGroup(groups)
                } else {
                    val specs = filtered[filtered.keys.first()]?.map { it.spec() }?.toImmutableList()
                    GroupedQuarterlies.TypeList(specs ?: persistentListOf())
                }
            }
        }

        return groupType
    }
}
