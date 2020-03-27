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

package com.cryart.sabbathschool.ui.languages

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.cryart.sabbathschool.R
import com.cryart.sabbathschool.extensions.view.inflateView

class LanguageItemHolder(private val containerView: View) : RecyclerView.ViewHolder(containerView) {

    private val textViewNativeName: TextView by lazy { containerView.findViewById<TextView>(R.id.ss_language_menu_item_title) }
    private val textViewName: TextView by lazy { containerView.findViewById<TextView>(R.id.ss_language_menu_item_name) }
    private val viewCheck: View by lazy { containerView.findViewById<View>(R.id.ss_language_menu_item_check) }

    fun bind(model: LanguageModel) {
        textViewNativeName.text = model.nativeName
        textViewName.text = model.name
        viewCheck.isVisible = model.selected
    }

    companion object {
        fun create(parent: ViewGroup): LanguageItemHolder = LanguageItemHolder(
                inflateView(R.layout.ss_language_item, parent, false)
        )
    }
}