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

package com.cryart.sabbathschool.lessons.ui.quarterlies

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.cryart.sabbathschool.core.extensions.arch.observeNonNull
import com.cryart.sabbathschool.core.extensions.view.setEdgeEffect
import com.cryart.sabbathschool.core.misc.SSColorTheme
import com.cryart.sabbathschool.core.misc.SSConstants
import com.cryart.sabbathschool.core.model.ViewState
import com.cryart.sabbathschool.core.navigation.AppNavigator
import com.cryart.sabbathschool.core.navigation.Destination
import com.cryart.sabbathschool.lessons.R
import com.cryart.sabbathschool.lessons.data.model.SSQuarterly
import com.cryart.sabbathschool.lessons.databinding.SsActivityQuarterliesBinding
import com.cryart.sabbathschool.lessons.ui.base.SSBaseActivity
import com.cryart.sabbathschool.lessons.ui.languages.LanguagesListFragment
import com.cryart.sabbathschool.lessons.ui.lessons.SSLessonsActivity
import dagger.hilt.android.AndroidEntryPoint
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt.STATE_DISMISSED
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt.STATE_FOCAL_PRESSED
import javax.inject.Inject

@AndroidEntryPoint
class QuarterliesActivity : SSBaseActivity() {

    @Inject
    lateinit var appNavigator: AppNavigator

    private val viewModel: QuarterliesViewModel by viewModels()

    private val binding: SsActivityQuarterliesBinding by lazy {
        SsActivityQuarterliesBinding.inflate(layoutInflater)
    }

    private val quarterliesAdapter: SSQuarterliesAdapter = SSQuarterliesAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupUi()

        viewModel.viewStateLiveData.observeNonNull(this) { state ->
            binding.apply {
                ssQuarterliesProgressBar.isVisible = state == ViewState.Loading
                ssQuarterliesList.isVisible = state is ViewState.Success<*>
                ssQuarterliesErrorState.isVisible = state is ViewState.Error
            }

            (state as? ViewState.Success<*>)?.let { bindQuarterlies(it) }
        }
        viewModel.showLanguagePromptLiveData.observe(this, { showLanguagesPrompt() })
        viewModel.lastQuarterlyIndexLiveData.observeNonNull(this) {
            val intent = Intent(this, SSLessonsActivity::class.java)
            intent.putExtra(SSConstants.SS_QUARTERLY_INDEX_EXTRA, it)
            startActivity(intent)
        }

        viewModel.viewCreated()
    }

    private fun setupUi() {
        val toolbar = binding.appBar.ssToolbar
        setSupportActionBar(toolbar)
        setupAccountToolbar(toolbar)
        updateColorScheme()
        supportActionBar?.apply {
            setDisplayShowCustomEnabled(true)
            setDisplayShowTitleEnabled(true)
        }
        toolbar.setNavigationOnClickListener {
            appNavigator.navigate(this, Destination.ACCOUNT)
        }

        binding.ssQuarterliesList.adapter = quarterliesAdapter
    }

    private fun bindQuarterlies(state: ViewState.Success<*>) {
        val dataList = state.data as? List<*> ?: return
        val quarterlies = dataList.filterIsInstance<SSQuarterly>()
            .takeIf { it.size == dataList.size } ?: emptyList()
        binding.ssQuarterliesEmpty.isVisible = quarterlies.isEmpty()

        if (quarterlies.isNotEmpty()) {
            SSColorTheme.getInstance(this).colorPrimary = quarterlies.first().color_primary
            SSColorTheme.getInstance(this).colorPrimaryDark =
                quarterlies.first().color_primary_dark
            updateColorScheme()
        }

        with(quarterliesAdapter) {
            setQuarterlies(quarterlies)
            notifyDataSetChanged()
        }
    }

    private fun updateColorScheme() {
        val primaryColor = Color.parseColor(SSColorTheme.getInstance(this).colorPrimary)
        binding.appBar.ssToolbar.setBackgroundColor(primaryColor)
        updateWindowColorScheme()
        binding.ssQuarterliesList.setEdgeEffect(primaryColor)
    }

    private fun showLanguagesPrompt() {
        MaterialTapTargetPrompt.Builder(this)
            .setTarget(R.id.ss_quarterlies_menu_filter)
            .setPrimaryText(getString(R.string.ss_quarterlies_filter_languages_prompt_title))
            .setCaptureTouchEventOutsidePrompt(true)
            .setIconDrawableColourFilter(
                Color.parseColor(
                    SSColorTheme.getInstance(this).colorPrimary
                )
            )
            .setIconDrawable(ContextCompat.getDrawable(this, R.drawable.ic_translate))
            .setBackgroundColour(
                Color.parseColor(
                    SSColorTheme.getInstance(this).colorPrimary
                )
            )
            .setSecondaryText(R.string.ss_quarterlies_filter_languages_prompt_description)
            .setPromptStateChangeListener { _, state ->
                if (state == STATE_DISMISSED || state == STATE_FOCAL_PRESSED) {
                    viewModel.languagesPromptSeen()
                }
            }
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.ss_quarterlies_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.ss_quarterlies_menu_filter) {
            val fragment = LanguagesListFragment.newInstance {
                viewModel.languageSelected(it)
            }
            fragment.show(supportFragmentManager, fragment.tag)
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}
