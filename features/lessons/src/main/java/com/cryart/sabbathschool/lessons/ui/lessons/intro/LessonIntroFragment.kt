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

package com.cryart.sabbathschool.lessons.ui.lessons.intro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.FragmentManager
import com.cryart.sabbathschool.core.extensions.view.viewBinding
import com.cryart.sabbathschool.lessons.R
import com.cryart.sabbathschool.lessons.databinding.SsFragmentLessonIntroBinding
import com.cryart.sabbathschool.lessons.ui.base.SsBottomSheetDialogFragment
import io.noties.markwon.Markwon

class LessonIntroFragment : SsBottomSheetDialogFragment() {

    private val binding by viewBinding(SsFragmentLessonIntroBinding::bind)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.ss_fragment_lesson_intro, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val model = arguments?.getParcelable<LessonIntroModel>(ARG_MODEL) ?: return

        binding.toolbar.apply {
            title = model.title
            setNavigationOnClickListener { dismiss() }
        }
        binding.scrollView.setOnScrollChangeListener { _: NestedScrollView?, _: Int,
            scrollY: Int, _: Int, _: Int ->
            binding.toolbar.isActivated = scrollY > 0
        }

        Markwon.create(requireContext()).setMarkdown(
            binding.txtContent,
            model.introduction
        )
    }
}

private const val ARG_MODEL = "arg:model"

internal fun FragmentManager.showLessonIntro(model: LessonIntroModel) {
    val fragment = LessonIntroFragment().apply {
        arguments = bundleOf(ARG_MODEL to model)
    }
    fragment.show(this, "LessonDescription")
}
