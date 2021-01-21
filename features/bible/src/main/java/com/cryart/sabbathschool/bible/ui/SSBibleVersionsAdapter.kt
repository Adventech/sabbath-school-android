/*
 * Copyright (c) 2016 Adventech <info@adventech.io>
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
package com.cryart.sabbathschool.bible.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.cryart.sabbathschool.bible.R
import com.cryart.sabbathschool.bible.databinding.SsBibleVersionItemBinding
import com.cryart.sabbathschool.bible.databinding.SsBibleVersionItemHeaderBinding
import com.cryart.sabbathschool.reader.data.model.SSBibleVerses

class SSBibleVersionsAdapter(private var ssBibleVerses: List<SSBibleVerses>) : BaseAdapter() {
    override fun getCount(): Int {
        return ssBibleVerses.size
    }

    override fun getItem(position: Int): SSBibleVerses {
        return ssBibleVerses[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, _view: View?, parent: ViewGroup): View {
        var view: View? = _view
        if (view == null) {
            val binding: SsBibleVersionItemHeaderBinding =
                DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.ss_bible_version_item_header, parent, false)
            view = binding.root
        }
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = getItem(position).name
        return view
    }

    override fun getDropDownView(position: Int, _view: View?, parent: ViewGroup): View {
        var view: View? = _view
        if (view == null) {
            val binding: SsBibleVersionItemBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.ss_bible_version_item, parent, false)
            view = binding.root
        }
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = getItem(position).name
        return view
    }
}
