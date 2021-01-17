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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.cryart.sabbathschool.lessons.R
import com.cryart.sabbathschool.lessons.ui.base.SsBottomSheetDialogFragment

class LessonTypesFragment : SsBottomSheetDialogFragment() {

    private var onTypeClick: ((String) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.ss_fragment_lesson_types, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val types = arguments?.getStringArrayList(ARG_TYPES) ?: return
        val listAdapter = TypesListAdapter(types) {
            onTypeClick?.invoke(it)
            dismiss()
        }
        val listView = view.findViewById<RecyclerView>(R.id.typesListView)
        listView.apply {
            addItemDecoration(
                DividerItemDecoration(context, RecyclerView.VERTICAL).apply {
                    setDrawable(ContextCompat.getDrawable(context, R.drawable.list_divider)!!)
                }
            )
            adapter = listAdapter
        }
    }

    companion object {
        private const val ARG_TYPES = "arg:types"

        fun newInstance(types: List<String>, onClick: (String) -> Unit): LessonTypesFragment {
            return LessonTypesFragment().apply {
                arguments = Bundle().apply {
                    putStringArrayList(ARG_TYPES, ArrayList(types))
                }
                onTypeClick = onClick
            }
        }
    }
}
