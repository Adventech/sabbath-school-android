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

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import app.ss.lessons.data.model.SSQuarterlyInfo
import com.cryart.design.theme
import com.cryart.sabbathschool.core.extensions.arch.observeNonNull
import com.cryart.sabbathschool.core.extensions.context.colorPrimary
import com.cryart.sabbathschool.core.extensions.context.colorPrimaryDark
import com.cryart.sabbathschool.core.extensions.prefs.SSPrefs
import com.cryart.sabbathschool.core.extensions.view.dividers
import com.cryart.sabbathschool.core.extensions.view.setEdgeEffect
import com.cryart.sabbathschool.core.extensions.view.viewBinding
import com.cryart.sabbathschool.core.misc.SSColorTheme
import com.cryart.sabbathschool.core.misc.SSConstants
import com.cryart.sabbathschool.core.navigation.AppNavigator
import com.cryart.sabbathschool.core.navigation.Destination
import com.cryart.sabbathschool.lessons.R
import com.cryart.sabbathschool.lessons.databinding.SsLessonsActivityBinding
import com.cryart.sabbathschool.lessons.ui.base.SSBaseActivity
import com.cryart.sabbathschool.lessons.ui.lessons.types.LessonTypesFragment
import dagger.hilt.android.AndroidEntryPoint
import hotchemi.android.rate.AppRate
import javax.inject.Inject

@AndroidEntryPoint
class SSLessonsActivity : SSBaseActivity(), SSLessonsViewModel.DataListener {

    @Inject
    lateinit var ssPrefs: SSPrefs

    @Inject
    lateinit var appNavigator: AppNavigator

    private var ssLessonsViewModel: SSLessonsViewModel? = null
    private val viewModel by viewModels<LessonsViewModel>()

    private val binding by viewBinding(SsLessonsActivityBinding::inflate)

    private val listAdapter = SSLessonsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        AppRate.with(this).setInstallDays(SSConstants.SS_APP_RATE_INSTALL_DAYS).monitor()
        AppRate.showRateDialogIfMeetsConditions(this)

        initUI()

        val index = intent.extras?.getString(SSConstants.SS_QUARTERLY_INDEX_EXTRA) ?: return
        ssLessonsViewModel = SSLessonsViewModel(this, ssPrefs, this, index)
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
        setSupportActionBar(binding.ssLessonsAppBar.ssLessonsToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.ssLessonsAppBar.ssLessonCollapsingToolbar
            .setCollapsedTitleTypeface(ResourcesCompat.getFont(this, R.font.lato_bold))
        binding.ssLessonsAppBar.ssLessonCollapsingToolbar
            .setExpandedTitleTypeface(ResourcesCompat.getFont(this, R.font.lato_bold))

        binding.ssLessonInfoList.apply {
            dividers(R.drawable.list_divider)
            adapter = listAdapter
        }

        binding.ssProgressBar.ssQuarterliesLoading.theme(colorPrimary)
    }

    private fun updateColorScheme() {
        val primaryColor = this.colorPrimary
        val primaryDarkColor = this.colorPrimaryDark

        binding.ssLessonsAppBar.apply {
            ssLessonsToolbar.setBackgroundColor(primaryColor)
            ssLessonCollapsingToolbar.setContentScrimColor(primaryColor)
            ssLessonCollapsingToolbar.setBackgroundColor(primaryColor)
            content.ssLessonsAppBarRead.backgroundTintList = ColorStateList.valueOf(primaryDarkColor)
        }
        binding.lessonTypeTextView.setTextColor(primaryColor)
        binding.ssLessonInfoList.setEdgeEffect(primaryColor)
        binding.ssProgressBar.ssQuarterliesLoading.theme(primaryColor)
        updateWindowColorScheme()
    }

    override fun onQuarterlyChanged(ssQuarterlyInfo: SSQuarterlyInfo) {
        SSColorTheme.getInstance(this).colorPrimary = ssQuarterlyInfo.quarterly.color_primary
        SSColorTheme.getInstance(this).colorPrimaryDark = ssQuarterlyInfo.quarterly
            .color_primary_dark
        updateColorScheme()
        binding.ssLessonsAppBar.ssLessonCollapsingToolbar.title = ssQuarterlyInfo.quarterly.title
        val adapter = binding.ssLessonInfoList.adapter as? SSLessonsAdapter?
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
                shareApp(ssLessonsViewModel?.ssQuarterlyInfo?.quarterly?.title)
                true
            }
            R.id.ss_lessons_menu_settings -> {
                appNavigator.navigate(this, Destination.SETTINGS)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
