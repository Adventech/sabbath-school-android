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

import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import app.ss.lessons.data.model.QuarterlyGroup
import app.ss.lessons.data.model.SSQuarterly
import com.cryart.design.ext.ComposeListAdapter
import com.cryart.design.ext.ComposeViewHolder
import com.cryart.design.ext.thenIf
import com.cryart.sabbathschool.core.extensions.coroutines.flow.collectIn
import com.cryart.sabbathschool.core.extensions.view.inflate
import com.cryart.sabbathschool.core.ui.BaseDataComponent
import com.cryart.sabbathschool.lessons.R
import com.cryart.sabbathschool.lessons.databinding.SsQuarterliesListBinding
import kotlinx.coroutines.flow.Flow

interface QuarterlyListCallbacks {
    fun onReadClick(index: String)
    fun onSeeAllClick(group: QuarterlyGroup)
}

/**
 * When this issue is closed we can remove [RecyclerView]
 * and use [androidx.compose.foundation.lazy.LazyColumn] instead:
 * https://issuetracker.google.com/issues/174348612
 */
class QuarterlyListComponent(
    lifecycleOwner: LifecycleOwner,
    private val binding: SsQuarterliesListBinding,
    callbacks: QuarterlyListCallbacks
) : BaseDataComponent<GroupedQuarterlies>(lifecycleOwner) {

    private val listAdapter = QuarterliesListAdapter(callbacks)
    private val groupListAdapter = QuarterliesGroupListAdapter(callbacks)

    override fun collect(dataFlow: Flow<GroupedQuarterlies>) {
        dataFlow.collectIn(owner) { data ->
            when (data) {
                GroupedQuarterlies.Empty -> {
                    listAdapter.submitList(emptyList())
                    groupListAdapter.submitList(emptyList())
                }
                is GroupedQuarterlies.TypeGroup -> {
                    if (binding.ssQuarterliesList.adapter != groupListAdapter) {
                        binding.ssQuarterliesList.adapter = groupListAdapter
                    }
                    groupListAdapter.submitList(data.data)
                }
                is GroupedQuarterlies.TypeList -> {
                    if (binding.ssQuarterliesList.adapter != listAdapter) {
                        binding.ssQuarterliesList.adapter = listAdapter
                    }
                    listAdapter.submitList(data.data)
                }
            }
        }
    }
}

private class QuarterliesListAdapter(
    private val callbacks: QuarterlyListCallbacks
) : ComposeListAdapter<SSQuarterly, QuarterlyViewHolder>(QuarterliesDiff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuarterlyViewHolder {
        return QuarterlyViewHolder(parent = parent, callbacks = callbacks)
    }

    override fun onBindViewHolder(holder: QuarterlyViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }
}

private class QuarterlyViewHolder(
    parent: ViewGroup,
    private val callbacks: QuarterlyListCallbacks
) : ComposeViewHolder(parent.inflate(R.layout.ss_compose_component)) {

    fun bind(item: SSQuarterly) = compose {
        QuarterlyRow(
            spec = item.spec(QuarterlySpec.Type.NORMAL),
            modifier = Modifier
                .thenIf(item.isPlaceholder.not()) {
                    clickable {
                        callbacks.onReadClick(item.index)
                    }
                },
        )
    }
}

private class QuarterliesGroupListAdapter(
    private val callbacks: QuarterlyListCallbacks
) : ComposeListAdapter<QuarterliesGroup, QuarterliesGroupViewHolder>(GroupedQuarterliesDiff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuarterliesGroupViewHolder {
        return QuarterliesGroupViewHolder(parent = parent, callbacks = callbacks)
    }

    override fun onBindViewHolder(holder: QuarterliesGroupViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, position == itemCount - 1)
    }
}

private class QuarterliesGroupViewHolder(
    parent: ViewGroup,
    private val callbacks: QuarterlyListCallbacks
) : ComposeViewHolder(parent.inflate(R.layout.ss_compose_component)) {

    fun bind(item: QuarterliesGroup, lastIndex: Boolean = false) = compose {
        val items = item.quarterlies.map { quarterly ->
            quarterly.spec(
                QuarterlySpec.Type.LARGE,
                onClick = {
                    callbacks.onReadClick(quarterly.index)
                }
            )
        }

        GroupedQuarterliesColumn(
            title = item.group.name,
            items = items,
            lastIndex
        ) {
            callbacks.onSeeAllClick(item.group)
        }
    }
}
