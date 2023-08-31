/*
 * Copyright (c) 2023. Adventech <info@adventech.io>
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
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.cryart.sabbathschool.lessons.ui.readings.components

import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.cryart.sabbathschool.core.extensions.context.colorPrimary
import com.cryart.sabbathschool.core.extensions.context.colorPrimaryDark
import com.cryart.sabbathschool.core.model.Status
import com.cryart.sabbathschool.lessons.databinding.SsReadingAppBarBinding
import kotlinx.coroutines.flow.Flow
import ss.foundation.coroutines.flow.collectIn

class AppBarComponent constructor(
    private val binding: SsReadingAppBarBinding,
    stateFlow: Flow<Status>,
    owner: LifecycleOwner,
) {

    init {
        binding.ssReadingCollapsingToolbar.run {
            // Replace with selected quarterly colors instead
            setContentScrimColor(context.colorPrimary)
            setStatusBarScrimColor(context.colorPrimaryDark)
        }

        stateFlow.collectIn(owner) { state ->
            val visibility = when (state) {
                Status.LOADING -> View.INVISIBLE
                Status.SUCCESS -> View.VISIBLE
                Status.ERROR -> View.GONE
            }
            binding.root.visibility = visibility
        }
    }
}
