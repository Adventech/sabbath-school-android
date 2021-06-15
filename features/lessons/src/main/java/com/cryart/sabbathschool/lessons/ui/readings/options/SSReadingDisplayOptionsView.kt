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
package com.cryart.sabbathschool.lessons.ui.readings.options

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.cryart.sabbathschool.core.extensions.prefs.SSPrefs
import com.cryart.sabbathschool.lessons.R
import com.cryart.sabbathschool.lessons.databinding.SsReadingDisplayOptionsBinding
import com.cryart.sabbathschool.lessons.ui.base.SsBottomSheetDialogFragment
import com.cryart.sabbathschool.lessons.ui.readings.SSReadingViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SSReadingDisplayOptionsView : SsBottomSheetDialogFragment() {

    interface ReadingDisplayOptions {
        fun getReadingViewModel(): SSReadingViewModel
    }

    @Inject
    lateinit var ssPrefs: SSPrefs

    private lateinit var readingDisplayOptions: ReadingDisplayOptions

    override fun onAttach(context: Context) {
        super.onAttach(context)
        readingDisplayOptions = context as ReadingDisplayOptions
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: SsReadingDisplayOptionsBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.ss_reading_display_options, null, false
        )
        binding.viewModel = SSReadingDisplayOptionsViewModel(
            binding,
            readingDisplayOptions.getReadingViewModel(),
            ssPrefs.getDisplayOptions()
        )

        return binding.root
    }
}
