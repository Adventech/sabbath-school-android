/*
 * Copyright (c) 2020 Adventech <info@adventech.io>
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

package com.cryart.sabbathschool.lessons.ui.lessons.types

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cryart.sabbathschool.core.extensions.view.inflateView
import com.cryart.sabbathschool.lessons.R

class TypesListAdapter(private val titles: List<String>,
                       private val onClick: (String) -> Unit) : RecyclerView.Adapter<TypesListAdapter.TypeHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TypeHolder {
        return TypeHolder.create(parent)
    }

    override fun getItemCount(): Int = titles.size

    override fun onBindViewHolder(holder: TypeHolder, position: Int) {
        val title = titles[position]
        holder.bind(title)

        holder.itemView.setOnClickListener {
            onClick(title)
        }
    }

    class TypeHolder(val view: View) : RecyclerView.ViewHolder(view) {

        private val titleView: TextView by lazy { view.findViewById(R.id.titleView) }

        fun bind(title: String) {
            titleView.text = title
        }

        companion object {
            fun create(parent: ViewGroup): TypeHolder = TypeHolder(
                    inflateView(R.layout.ss_lesson_type_item, parent, false)
            )
        }
    }
}
