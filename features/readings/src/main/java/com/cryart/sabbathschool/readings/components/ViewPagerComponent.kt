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

package com.cryart.sabbathschool.readings.components

import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.viewpager2.widget.ViewPager2
import com.cryart.sabbathschool.core.extensions.coroutines.observeOnLifecycle
import com.cryart.sabbathschool.readings.components.model.ReadingDaysData
import com.cryart.sabbathschool.readings.databinding.ComponentReadingPagerBinding
import com.cryart.sabbathschool.readings.days.ReadingDaysPagerAdapter
import kotlinx.coroutines.flow.Flow

class ViewPagerComponent(
    activity: FragmentActivity,
    private val binding: ComponentReadingPagerBinding,
    pageSelected: (Int) -> Unit
) : BaseComponent<ReadingDaysData> {

    private val pagerAdapter = ReadingDaysPagerAdapter(activity)

    init {
        binding.viewPager.apply {
            adapter = pagerAdapter
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    pageSelected(position)
                }
            })
        }
    }

    override fun show() {
        binding.viewPager.isVisible = true
    }

    override fun hide() {
        binding.viewPager.isVisible = false
    }

    override fun collect(flow: Flow<ReadingDaysData>, owner: LifecycleOwner) {
        flow.observeOnLifecycle(owner) { data ->
            pagerAdapter.days = data.days
        }
    }
}
