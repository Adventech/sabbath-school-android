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

package com.cryart.sabbathschool.lessons.ui.quarterlies.list

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.view.isVisible
import app.ss.lessons.data.model.QuarterlyGroup
import com.cryart.sabbathschool.core.extensions.activity.slideEnter
import com.cryart.sabbathschool.core.extensions.activity.startIntentWithScene
import com.cryart.sabbathschool.core.extensions.coroutines.flow.collectIn
import com.cryart.sabbathschool.core.extensions.view.viewBinding
import com.cryart.sabbathschool.core.misc.SSConstants
import com.cryart.sabbathschool.core.model.Status
import com.cryart.sabbathschool.lessons.databinding.SsActivityQuarterliesBinding
import com.cryart.sabbathschool.lessons.ui.base.SSBaseActivity
import com.cryart.sabbathschool.lessons.ui.lessons.SSLessonsActivity
import com.cryart.sabbathschool.lessons.ui.quarterlies.QuarterliesViewModel
import com.cryart.sabbathschool.lessons.ui.quarterlies.components.GroupedQuarterlies
import com.cryart.sabbathschool.lessons.ui.quarterlies.components.QuarterliesAppbarComponent
import com.cryart.sabbathschool.lessons.ui.quarterlies.components.QuarterlyListCallbacks
import com.cryart.sabbathschool.lessons.ui.quarterlies.components.QuarterlyListComponent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.map

@AndroidEntryPoint
class QuarterliesListActivity : SSBaseActivity(), QuarterlyListCallbacks {

    private val viewModel by viewModels<QuarterliesViewModel>()

    private val binding by viewBinding(SsActivityQuarterliesBinding::inflate)

    private val appbarComponent: QuarterliesAppbarComponent by lazy {
        QuarterliesAppbarComponent(this, binding.appBar)
    }
    private val listComponent: QuarterlyListComponent by lazy {
        QuarterlyListComponent(this, binding.ssQuarterliesList, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        slideEnter()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        collectData()
    }

    private fun collectData() {
        val stateFlow = viewModel.quarterliesFlow.map { it.status }
        stateFlow.collectIn(this) { status ->
            binding.apply {
                ssQuarterliesErrorState.isVisible = status == Status.ERROR
            }
        }
        appbarComponent.collect(viewModel.groupTitleFlow)
        listComponent.collect(
            viewModel.quarterliesFlow.map { it.data ?: GroupedQuarterlies.Empty }
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finishAfterTransition()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onReadClick(index: String) {
        val lessonsIntent = SSLessonsActivity.launchIntent(this, index)
        startIntentWithScene(lessonsIntent)
    }

    override fun onSeeAllClick(group: QuarterlyGroup) {
        // No op
    }

    companion object {
        fun launchIntent(
            context: Context,
            group: QuarterlyGroup
        ): Intent = Intent(
            context, QuarterliesListActivity::class.java
        ).apply {
            putExtra(SSConstants.SS_QUARTERLY_GROUP, group)
        }
    }
}
