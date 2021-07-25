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

import androidx.lifecycle.LifecycleOwner
import com.cryart.sabbathschool.core.extensions.context.colorPrimary
import com.cryart.sabbathschool.core.extensions.coroutines.flow.collectIn
import com.cryart.sabbathschool.core.extensions.view.fadeTo
import com.cryart.sabbathschool.core.ui.BaseComponent
import com.cryart.sabbathschool.readings.components.model.ErrorData
import com.cryart.sabbathschool.readings.databinding.ComponentLessonErrorBinding
import kotlinx.coroutines.flow.Flow

class LessonErrorComponent(
    lifecycleOwner: LifecycleOwner,
    private val binding: ComponentLessonErrorBinding,
    val onClose: () -> Unit
) : BaseComponent<ErrorData>(lifecycleOwner) {

    init {
        binding.backdrop.apply {
            setBackgroundColor(context.colorPrimary)
        }
        binding.navClose.setOnClickListener {
            onClose()
        }
    }

    override fun collect(visibilityFlow: Flow<Boolean>, dataFlow: Flow<ErrorData>) {
        visibilityFlow.collectIn(owner) { visible ->
            binding.errorView.fadeTo(visible)
        }
        dataFlow.collectIn(owner) { data ->
            when (data) {
                is ErrorData.Data -> {
                    binding.apply {
                        errorIcon.setImageResource(data.iconRes)
                        errorTitle.text = if (!data.error.isNullOrEmpty()) {
                            data.error
                        } else if (data.errorRes != null) {
                            errorTitle.resources.getString(data.errorRes)
                        } else {
                            ""
                        }
                    }
                }
                ErrorData.Empty -> {
                }
            }
        }
    }
}
