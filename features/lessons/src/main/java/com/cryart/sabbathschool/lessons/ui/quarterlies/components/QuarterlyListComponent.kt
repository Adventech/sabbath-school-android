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

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import app.ss.lessons.data.model.QuarterlyGroup
import app.ss.lessons.data.model.SSQuarterly
import com.cryart.design.ext.ComposeRecyclerViewAdapter
import com.cryart.design.ext.ComposeViewHolder
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
    binding: SsQuarterliesListBinding,
    callbacks: QuarterlyListCallbacks
) : BaseDataComponent<GroupedQuarterlies>(lifecycleOwner) {

    private val listAdapter = QuarterliesListAdapter(callbacks)

    init {
        binding.ssQuarterliesList.adapter = listAdapter
    }

    override fun collect(dataFlow: Flow<GroupedQuarterlies>) {
        dataFlow.collectIn(owner) { data ->
            listAdapter.quarterlies = data
        }
    }
}

private class QuarterliesListAdapter(
    private val callbacks: QuarterlyListCallbacks
) : ComposeRecyclerViewAdapter<QuarterlyViewHolder>() {

    var quarterlies: GroupedQuarterlies? = null
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuarterlyViewHolder {
        return QuarterlyViewHolder.create(parent = parent, callbacks = callbacks)
    }

    override fun onBindViewHolder(holder: QuarterlyViewHolder, position: Int) {
        when (quarterlies) {
            is GroupedQuarterlies.TypeList -> {
                val item = (quarterlies as? GroupedQuarterlies.TypeList)?.data?.getOrNull(position) ?: return
                holder.bind(item)
            }
            is GroupedQuarterlies.TypeMap -> {
                val data = (quarterlies as? GroupedQuarterlies.TypeMap)?.data ?: return
                val key = data.keys.elementAt(position)
                val lastIndex = position == itemCount - 1
                holder.bind(key, data[key] ?: emptyList(), lastIndex)
            }
            else -> return
        }
    }

    override fun getItemCount(): Int {
        return when (quarterlies) {
            is GroupedQuarterlies.TypeList -> (quarterlies as? GroupedQuarterlies.TypeList)?.data?.size ?: 0
            is GroupedQuarterlies.TypeMap -> (quarterlies as? GroupedQuarterlies.TypeMap)?.data?.keys?.size ?: 0
            else -> 0
        }
    }
}

private class QuarterlyViewHolder(
    view: ComposeView,
    private val callbacks: QuarterlyListCallbacks
) : ComposeViewHolder(view) {

    fun bind(item: SSQuarterly) = compose {
        QuarterlyRow(
            spec = item.spec(QuarterlySpec.Type.NORMAL),
            modifier = Modifier
                .clickable {
                    callbacks.onReadClick(item.index)
                }
        )
    }

    fun bind(group: QuarterlyGroup, data: List<SSQuarterly>, lastIndex: Boolean = false) = compose {
        val title = group.name
        val items = data.map { quarterly ->
            quarterly.spec(
                QuarterlySpec.Type.LARGE,
                onClick = {
                    callbacks.onReadClick(quarterly.index)
                }
            )
        }

        GroupedQuarterliesColumn(
            title = title,
            items = items,
            lastIndex
        )
    }

    companion object {
        fun create(
            parent: ViewGroup,
            callbacks: QuarterlyListCallbacks
        ): QuarterlyViewHolder = QuarterlyViewHolder(
            parent.inflate(R.layout.ss_compose_component),
            callbacks
        )
    }
}
