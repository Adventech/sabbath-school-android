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
package com.cryart.sabbathschool.lessons.ui.lessons

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import androidx.core.widget.NestedScrollView
import com.cryart.sabbathschool.core.extensions.context.shareContent
import com.cryart.sabbathschool.core.extensions.context.toWebUri
import com.cryart.sabbathschool.core.extensions.view.viewBinding
import com.cryart.sabbathschool.core.misc.SSConstants
import com.cryart.sabbathschool.core.navigation.AppNavigator
import com.cryart.sabbathschool.core.navigation.Destination
import com.cryart.sabbathschool.lessons.R
import com.cryart.sabbathschool.lessons.databinding.SsLessonsActivityBinding
import com.cryart.sabbathschool.lessons.ui.base.SSBaseActivity
import com.cryart.sabbathschool.lessons.ui.base.ShareableScreen
import com.cryart.sabbathschool.lessons.ui.lessons.components.LessonTypeComponent
import com.cryart.sabbathschool.lessons.ui.lessons.components.LessonsListComponent
import com.cryart.sabbathschool.lessons.ui.lessons.components.QuarterlyInfoComponent
import com.cryart.sabbathschool.lessons.ui.lessons.components.ToolbarComponent
import dagger.hilt.android.AndroidEntryPoint
import hotchemi.android.rate.AppRate
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@AndroidEntryPoint
class SSLessonsActivity : SSBaseActivity(), ShareableScreen {

    @Inject
    lateinit var appNavigator: AppNavigator

    private val viewModel by viewModels<LessonsViewModel>()

    private val binding by viewBinding(SsLessonsActivityBinding::inflate)

    private val toolbarComponent: ToolbarComponent by lazy {
        ToolbarComponent(this, binding.ssLessonsToolbar)
    }
    private val quarterlyInfoComponent: QuarterlyInfoComponent by lazy {
        QuarterlyInfoComponent(this, binding.appBarContent)
    }
    private val lessonTypeComponent: LessonTypeComponent by lazy {
        LessonTypeComponent(
            this,
            binding.lessonTypeContainer,
            supportFragmentManager,
            viewModel::quarterlyTypeSelected
        )
    }
    private val lessonsListComponent: LessonsListComponent by lazy {
        LessonsListComponent(this, binding.ssLessonInfoList)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        AppRate.with(this).setInstallDays(SSConstants.SS_APP_RATE_INSTALL_DAYS).monitor()
        AppRate.showRateDialogIfMeetsConditions(this)

        initUI()
        collectData()
    }

    private fun initUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setSupportActionBar(binding.ssLessonsToolbar.ssLessonsToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.scrollView.setOnScrollChangeListener { _: NestedScrollView?, _: Int,
            scrollY: Int, _: Int, _: Int ->
            val contentHeight = binding.appBarContent.root.height
            toolbarComponent.onContentScroll(scrollY, contentHeight, this)
        }
    }

    private fun collectData() {
        val dataFlow = viewModel.quarterlyInfoFlow.map { it.data }

        toolbarComponent.collect(emptyFlow(), dataFlow.map { it?.quarterly?.title })

        quarterlyInfoComponent.collect(emptyFlow(), dataFlow)

        lessonTypeComponent.collect(emptyFlow(), viewModel.lessonTypesFlow)

        val lessonsFlow = dataFlow.map { it?.lessons ?: emptyList() }
        lessonsListComponent.collect(emptyFlow(), lessonsFlow)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.ss_lessons_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finishAfterTransition()
                true
            }
            R.id.ss_lessons_menu_share -> {
                val message = viewModel.quarterlyTitle
                shareContent(
                    "$message\n${getShareWebUri()}",
                    getString(R.string.ss_menu_share_app)
                )
                true
            }
            R.id.ss_lessons_menu_settings -> {
                appNavigator.navigate(this, Destination.SETTINGS)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun getShareWebUri(): Uri {
        return "${getString(R.string.ss_app_host)}/${viewModel.quarterlyShareIndex}".toWebUri()
    }

    companion object {

        fun launchIntent(
            context: Context,
            quarterlyIndex: String
        ): Intent = Intent(
            context,
            SSLessonsActivity::class.java
        ).apply {
            putExtra(SSConstants.SS_QUARTERLY_INDEX_EXTRA, quarterlyIndex)
        }
    }
}
