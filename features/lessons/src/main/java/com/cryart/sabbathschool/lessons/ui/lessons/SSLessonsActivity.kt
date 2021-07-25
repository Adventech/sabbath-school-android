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
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat.Type.systemBars
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import app.ss.lessons.data.model.SSQuarterlyInfo
import app.ss.widgets.AppWidgetHelper
import com.cryart.design.dividers
import com.cryart.design.setEdgeEffect
import com.cryart.design.theme
import com.cryart.sabbathschool.core.extensions.arch.observeNonNull
import com.cryart.sabbathschool.core.extensions.context.colorPrimary
import com.cryart.sabbathschool.core.extensions.context.colorPrimaryTint
import com.cryart.sabbathschool.core.extensions.context.shareContent
import com.cryart.sabbathschool.core.extensions.context.toWebUri
import com.cryart.sabbathschool.core.extensions.prefs.SSPrefs
import com.cryart.sabbathschool.core.extensions.view.doOnApplyWindowInsets
import com.cryart.sabbathschool.core.extensions.view.viewBinding
import com.cryart.sabbathschool.core.misc.SSColorTheme
import com.cryart.sabbathschool.core.misc.SSConstants
import com.cryart.sabbathschool.core.model.Status
import com.cryart.sabbathschool.core.navigation.AppNavigator
import com.cryart.sabbathschool.core.navigation.Destination
import com.cryart.sabbathschool.lessons.R
import com.cryart.sabbathschool.lessons.databinding.SsLessonsActivityBinding
import com.cryart.sabbathschool.lessons.ui.base.SSBaseActivity
import com.cryart.sabbathschool.lessons.ui.base.ShareableScreen
import com.cryart.sabbathschool.lessons.ui.lessons.components.QuarterlyInfoComponent
import com.cryart.sabbathschool.lessons.ui.lessons.types.LessonTypesFragment
import dagger.hilt.android.AndroidEntryPoint
import hotchemi.android.rate.AppRate
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class SSLessonsActivity : SSBaseActivity(), SSLessonsViewModel.DataListener, ShareableScreen {

    @Inject
    lateinit var ssPrefs: SSPrefs

    @Inject
    lateinit var appNavigator: AppNavigator

    @Inject
    lateinit var appWidgetHelper: AppWidgetHelper

    private var ssLessonsViewModel: SSLessonsViewModel? = null
    private val viewModel by viewModels<LessonsViewModel>()

    private val binding by viewBinding(SsLessonsActivityBinding::inflate)

    private val quarterlyInfoComponent: QuarterlyInfoComponent by lazy {
        QuarterlyInfoComponent(this, binding.appBarContent)
    }

    private val listAdapter = SSLessonsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        AppRate.with(this).setInstallDays(SSConstants.SS_APP_RATE_INSTALL_DAYS).monitor()
        AppRate.showRateDialogIfMeetsConditions(this)

        initUI()

        val index = intent.extras?.getString(SSConstants.SS_QUARTERLY_INDEX_EXTRA) ?: ssPrefs.getLastQuarterlyIndex()
        if (index == null) {
            finish()
            return
        }

        ssLessonsViewModel = SSLessonsViewModel(ssPrefs, this, index, appWidgetHelper)
        binding.executePendingBindings()
        binding.viewModel = ssLessonsViewModel

        viewModel.quarterlyTypesLiveData.observeNonNull(
            this,
            { types ->
                if (binding.ssLessonInfoList.childCount > 0) {
                    updateLessonTypesLabel(types)
                } else {
                    listAdapter.registerAdapterDataObserver(
                        object : RecyclerView.AdapterDataObserver() {
                            override fun onChanged() {
                                super.onChanged()
                                listAdapter.unregisterAdapterDataObserver(this)
                                updateLessonTypesLabel(types)
                            }
                        })
                }
            }
        )
        viewModel.selectedTypeLiveData.observeNonNull(this) {
            val newIndex = it.first
            val type = it.second

            binding.lessonTypeTextView.text = type
            ssLessonsViewModel?.setSsQuarterlyIndex(newIndex)
        }
        viewModel.setQuarterlyIndex(index)
    }

    private fun updateLessonTypesLabel(types: List<String>) {
        binding.lessonTypeContainer.isVisible = types.isNotEmpty()
        if (types.isNotEmpty()) {
            binding.lessonTypeTextView.text = types.first()
            binding.lessonTypeContainer.setOnClickListener {
                val fragment = LessonTypesFragment.newInstance(types) {
                    viewModel.quarterlyTypeSelected(it)
                }
                fragment.show(supportFragmentManager, fragment.tag)
            }
        }
    }

    private fun initUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setSupportActionBar(binding.ssLessonsToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.ssLessonsToolbar.apply {
            fitsSystemWindows = false
            doOnApplyWindowInsets { insetView, windowInsets, _, initialMargins ->
                insetView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    updateMargins(top = initialMargins.top + windowInsets.getInsets(systemBars()).top)
                }
            }
        }

        binding.scrollView.setOnScrollChangeListener { _: NestedScrollView?, _: Int,
            scrollY: Int, _: Int, _: Int ->
            // TODO: Toolbar background on Scroll
            Timber.d("SCROLL: $scrollY, H: ${binding.appBarContent.root.height}")
        }

        binding.ssLessonInfoList.apply {
            dividers()
            adapter = listAdapter
        }

        binding.ssProgressBar.ssQuarterliesLoading.theme(colorPrimary)

        val visibilityFlow = viewModel.quarterlyInfoFlow.map { it.status == Status.SUCCESS }
        val dataFlow = viewModel.quarterlyInfoFlow.map { it.data }
        quarterlyInfoComponent.collect(visibilityFlow, dataFlow)
    }

    private fun updateColorScheme() {
        val primaryColor = this.colorPrimary

        binding.lessonTypeTextView.setTextColor(this.colorPrimaryTint)
        binding.ssLessonInfoList.setEdgeEffect(primaryColor)
        binding.ssProgressBar.ssQuarterliesLoading.theme(primaryColor)
    }

    override fun onQuarterlyChanged(ssQuarterlyInfo: SSQuarterlyInfo) {
        SSColorTheme.getInstance(this).colorPrimary = ssQuarterlyInfo.quarterly.color_primary
        SSColorTheme.getInstance(this).colorPrimaryDark = ssQuarterlyInfo.quarterly
            .color_primary_dark
        updateColorScheme()
        val adapter = binding.ssLessonInfoList.adapter as? SSLessonsAdapter
        adapter?.setLessons(ssQuarterlyInfo.lessons)
        adapter?.notifyDataSetChanged()
        binding.invalidateAll()
        binding.executePendingBindings()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.ss_lessons_menu, menu)
        return true
    }

    override fun onDestroy() {
        ssLessonsViewModel?.destroy()
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finishAfterTransition()
                true
            }
            R.id.ss_lessons_menu_share -> {
                val message = ssLessonsViewModel?.quarterlyTitle ?: ""
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
        return "${getString(R.string.ss_app_host)}/${ssLessonsViewModel?.quarterlyShareIndex ?: ""}".toWebUri()
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
