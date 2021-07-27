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

import android.graphics.Color
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import com.cryart.sabbathschool.core.extensions.context.isDarkTheme
import com.cryart.sabbathschool.core.extensions.coroutines.flow.collectIn
import com.cryart.sabbathschool.core.extensions.view.fadeTo
import com.cryart.sabbathschool.core.ui.BaseDataComponent
import com.cryart.sabbathschool.lessons.databinding.SsLessonTypeBinding
import com.cryart.sabbathschool.lessons.ui.lessons.types.LessonTypesFragment
import kotlinx.coroutines.flow.Flow

data class LessonTypeModel(
    val selected: String,
    val selectedPrimaryColor: String,
    val list: List<String>
)

class LessonTypeComponent constructor(
    lifecycleOwner: LifecycleOwner,
    private val binding: SsLessonTypeBinding,
    private val fragmentManager: FragmentManager,
    private val typeSelected: (String) -> Unit
) : BaseDataComponent<LessonTypeModel?>(lifecycleOwner) {

    override fun collect(dataFlow: Flow<LessonTypeModel?>) {
        dataFlow.collectIn(owner) { data ->
            binding.lessonTypeContainer.fadeTo(data != null)

            data?.let { model ->
                binding.lessonTypeTextView.apply {
                    text = model.selected
                    setTextColor(if (context.isDarkTheme()) Color.WHITE else Color.parseColor(model.selectedPrimaryColor))
                }
                binding.lessonTypeContainer.setOnClickListener {
                    val fragment = LessonTypesFragment.newInstance(model.list) {
                        typeSelected(it)
                    }
                    fragment.show(fragmentManager, fragment.tag)
                }
            }
        }
    }
}
