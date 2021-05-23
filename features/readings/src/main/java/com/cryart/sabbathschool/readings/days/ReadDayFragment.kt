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

package com.cryart.sabbathschool.readings.days

import android.graphics.Color
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.cryart.sabbathschool.core.extensions.prefs.SSPrefs
import com.cryart.sabbathschool.core.extensions.view.viewBinding
import com.cryart.sabbathschool.core.ui.SSBaseFragment
import com.cryart.sabbathschool.readings.R
import com.cryart.sabbathschool.readings.components.model.ReadingDay
import com.cryart.sabbathschool.readings.databinding.FragmentDayBinding
import com.cryart.sabbathschool.readings.days.components.ReadingViewComponent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject

@AndroidEntryPoint
class ReadDayFragment : SSBaseFragment<FragmentDayBinding>(R.layout.fragment_day) {

    @Inject
    lateinit var ssPrefs: SSPrefs

    private val viewModel by viewModels<ReadingDayViewModel>()
    override val viewBinding by viewBinding(FragmentDayBinding::bind)

    private var readingViewComponent: ReadingViewComponent? = null

    override fun onInit() {
        super.onInit()
        val options = ssPrefs.getDisplayOptions()
        view?.setBackgroundColor(Color.parseColor(options.rgb))
        readingViewComponent = ReadingViewComponent(options, viewBinding.readingView)
    }

    override fun onPostInit() {
        super.onPostInit()
        val day = arguments?.getParcelable(ARG_DAY) as? ReadingDay ?: return
        viewModel.loadData(day)
    }

    override fun onBindViewModel() {
        super.onBindViewModel()
        readingViewComponent?.collect(emptyFlow(), viewModel.readDataFlow, this)
    }

    companion object {
        private const val ARG_DAY = "arg:day"

        fun newInstance(day: ReadingDay): ReadDayFragment = ReadDayFragment().apply {
            arguments = bundleOf(ARG_DAY to day)
        }
    }
}
