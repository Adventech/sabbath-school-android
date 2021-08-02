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

package com.cryart.sabbathschool.lessons.ui.lessons.components

import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.ss.lessons.data.model.SSLesson
import com.cryart.design.dividers
import com.cryart.sabbathschool.core.extensions.coroutines.flow.collectIn
import com.cryart.sabbathschool.core.extensions.view.inflate
import com.cryart.sabbathschool.core.misc.SSConstants
import com.cryart.sabbathschool.core.ui.BaseDataComponent
import com.cryart.sabbathschool.lessons.R
import com.cryart.sabbathschool.lessons.databinding.SsLessonItemBinding
import com.cryart.sabbathschool.lessons.databinding.SsLessonsListBinding
import com.cryart.sabbathschool.lessons.ui.readings.SSReadingActivity
import kotlinx.coroutines.flow.Flow
import org.joda.time.format.DateTimeFormat

class LessonsListComponent constructor(
    lifecycleOwner: LifecycleOwner,
    binding: SsLessonsListBinding
) : BaseDataComponent<List<SSLesson>>(lifecycleOwner) {

    private val listAdapter = LessonsListAdapter()

    init {
        binding.ssLessonInfoList.apply {
            dividers()
            adapter = listAdapter
        }
    }

    override fun collect(dataFlow: Flow<List<SSLesson>>) {
        dataFlow.collectIn(owner) { data ->
            listAdapter.submitList(data)
        }
    }
}

private class LessonsListAdapter : ListAdapter<SSLesson, LessonInfoHolder>(object : DiffUtil.ItemCallback<SSLesson>() {
    override fun areItemsTheSame(oldItem: SSLesson, newItem: SSLesson): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: SSLesson, newItem: SSLesson): Boolean {
        return oldItem == newItem
    }
}) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LessonInfoHolder {
        return LessonInfoHolder.create(parent).also { holder ->
            holder.itemView.setOnClickListener { view ->
                val position = holder.absoluteAdapterPosition
                val item = getItem(position)

                val ssReadingIntent = SSReadingActivity.launchIntent(view.context, item.index)
                view.context.startActivity(ssReadingIntent)
            }
        }
    }

    override fun onBindViewHolder(holder: LessonInfoHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }
}

private class LessonInfoHolder(private val binding: SsLessonItemBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: SSLesson) {
        binding.ssLessonItemIndex.text = absoluteAdapterPosition.plus(1).toString()
        binding.ssLessonItemTitle.text = item.title
        binding.ssLessonItemNormalDate.text = item.dateDisplay()
    }

    private fun SSLesson.dateDisplay(): String {
        val startDateOut = DateTimeFormat.forPattern(SSConstants.SS_DATE_FORMAT_OUTPUT)
            .print(
                DateTimeFormat.forPattern(SSConstants.SS_DATE_FORMAT)
                    .parseLocalDate(start_date)
            )

        val endDateOut = DateTimeFormat.forPattern(SSConstants.SS_DATE_FORMAT_OUTPUT)
            .print(
                DateTimeFormat.forPattern(SSConstants.SS_DATE_FORMAT)
                    .parseLocalDate(end_date)
            )

        return "$startDateOut - $endDateOut".replaceFirstChar { it.uppercase() }
    }

    companion object {
        fun create(parent: ViewGroup): LessonInfoHolder = LessonInfoHolder(
            SsLessonItemBinding.bind(parent.inflate(R.layout.ss_lesson_item))
        )
    }
}
