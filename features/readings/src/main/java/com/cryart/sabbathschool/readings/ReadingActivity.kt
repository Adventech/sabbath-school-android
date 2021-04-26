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

package com.cryart.sabbathschool.readings

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import com.cryart.sabbathschool.core.extensions.view.viewBinding
import com.cryart.sabbathschool.core.misc.SSConstants
import com.cryart.sabbathschool.core.model.Status
import com.cryart.sabbathschool.core.ui.SSColorSchemeActivity
import com.cryart.sabbathschool.readings.components.AppBarComponent
import com.cryart.sabbathschool.readings.components.LessonErrorComponent
import com.cryart.sabbathschool.readings.components.LessonLoadingComponent
import com.cryart.sabbathschool.readings.components.ViewPagerComponent
import com.cryart.sabbathschool.readings.databinding.ActivityReadingBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.map

@AndroidEntryPoint
class ReadingActivity : SSColorSchemeActivity() {

    private val viewModel by viewModels<ReadingViewModel>()
    private val binding by viewBinding(ActivityReadingBinding::inflate)

    private val appBarComponent: AppBarComponent by lazy {
        AppBarComponent(binding.appBar)
    }
    private val viewPagerComponent: ViewPagerComponent by lazy {
        ViewPagerComponent(this, binding.viewPager, viewModel::onPageSelected)
    }
    private val loadingComponent: LessonLoadingComponent by lazy {
        LessonLoadingComponent(binding.loadingView)
    }
    private val errorComponent: LessonErrorComponent by lazy {
        LessonErrorComponent(binding.errorView, this::finish)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val lessonIndex = intent.extras?.getString(SSConstants.SS_LESSON_INDEX_EXTRA)
        if (lessonIndex.isNullOrEmpty()) {
            finish()
            return
        }

        initUI()

        observeItems()

        viewModel.loadData(lessonIndex)
    }

    private fun initUI() {
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE

        setSupportActionBar(binding.appBar.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
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

    private fun observeItems() {
        // Visible when Error
        val errorFlow = viewModel.uiStateFlow.map { it == Status.ERROR }
        // Visible when Success
        val successFlow = viewModel.uiStateFlow.map { it == Status.SUCCESS }
        // Visible when Loading
        val loadingFlow = viewModel.uiStateFlow.map { it == Status.LOADING }

        errorComponent.collect(errorFlow, viewModel.errorDataFlow, this)
        appBarComponent.collect(successFlow, viewModel.appBarDataFlow, this)
        viewPagerComponent.collect(successFlow, viewModel.readDaysFlow, this)
        loadingComponent.collect(loadingFlow, this)
    }

    override fun onStop() {
        viewModel.saveSelectedPage(viewPagerComponent.currPage)
        super.onStop()
    }
}
