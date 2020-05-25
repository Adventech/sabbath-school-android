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

package com.cryart.sabbathschool.ui.quarterlies

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.cryart.sabbathschool.R
import com.cryart.sabbathschool.data.di.ViewModelFactory
import com.cryart.sabbathschool.data.model.Status
import com.cryart.sabbathschool.extensions.arch.observeNonNull
import com.cryart.sabbathschool.misc.SSColorTheme
import com.cryart.sabbathschool.misc.SSConstants
import com.cryart.sabbathschool.ui.languages.LanguagesListFragment
import com.cryart.sabbathschool.ui.lessons.SSLessonsActivity
import com.cryart.sabbathschool.view.SSBaseActivity
import dagger.android.AndroidInjection
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt.STATE_DISMISSED
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt.STATE_FOCAL_PRESSED
import java.util.Locale
import javax.inject.Inject

class QuarterliesActivity : SSBaseActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: QuarterliesViewModel by viewModels { viewModelFactory }

    private val toolbar: Toolbar by lazy { findViewById<Toolbar>(R.id.ss_toolbar) }
    private val quarterliesListView: RecyclerView by lazy { findViewById<RecyclerView>(R.id.ss_quarterlies_list) }
    private val progressView: View by lazy { findViewById<View>(R.id.ss_quarterlies_progress_bar) }
    private val emptyView: View by lazy { findViewById<View>(R.id.ss_quarterlies_empty) }
    private val errorView: View by lazy { findViewById<View>(R.id.ss_quarterlies_error_state) }

    private lateinit var quarterliesAdapter: SSQuarterliesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ss_activity_quarterlies)

        setupUi()

        viewModel.viewStatusLiveData.observeNonNull(this) {
            progressView.isVisible = it == Status.LOADING
            quarterliesListView.isVisible = it == Status.SUCCESS
            errorView.isVisible = it == Status.ERROR
        }
        viewModel.quarterliesLiveData.observeNonNull(this) { quarterlies ->
            emptyView.isVisible = quarterlies.isEmpty()

            if (quarterlies.isNotEmpty()) {
                SSColorTheme.getInstance().colorPrimary = quarterlies.first().color_primary
                SSColorTheme.getInstance().colorPrimaryDark = quarterlies.first().color_primary_dark
                updateColorScheme()
            }

            with(quarterliesAdapter) {
                setQuarterlies(quarterlies)
                notifyDataSetChanged()
            }
        }
        viewModel.showLanguagePromptLiveData.observe(this, Observer {
            showLanguagesPrompt()
        })
        viewModel.lastQuarterlyIndexLiveData.observeNonNull(this) {
            val intent = Intent(this, SSLessonsActivity::class.java)
            intent.putExtra(SSConstants.SS_QUARTERLY_INDEX_EXTRA, it)
            startActivity(intent)
        }

        viewModel.viewCreated(Locale.getDefault().language)
    }

    private fun setupUi() {
        setSupportActionBar(toolbar)
        setupAccountToolbar(toolbar)
        supportActionBar?.apply {
            setDisplayShowCustomEnabled(true)
            setDisplayShowTitleEnabled(true)
        }

        quarterliesAdapter = SSQuarterliesAdapter()
        quarterliesListView.adapter = quarterliesAdapter
    }

    private fun updateColorScheme() {
        val primaryColor = Color.parseColor(SSColorTheme.getInstance().colorPrimary)
        toolbar.setBackgroundColor(primaryColor)
        updateWindowColorScheme()
    }

    private fun showLanguagesPrompt() {
        MaterialTapTargetPrompt.Builder(this)
                .setTarget(R.id.ss_quarterlies_menu_filter)
                .setPrimaryText(getString(R.string.ss_quarterlies_filter_languages_prompt_title))
                .setCaptureTouchEventOutsidePrompt(true)
                .setIconDrawableColourFilter(Color.parseColor(SSColorTheme.getInstance().colorPrimary))
                .setIconDrawable(ContextCompat.getDrawable(this, R.drawable.ic_translate))
                .setBackgroundColour(Color.parseColor(SSColorTheme.getInstance().colorPrimary))
                .setSecondaryText(R.string.ss_quarterlies_filter_languages_prompt_description)
                .setPromptStateChangeListener { _, state ->
                    if (state == STATE_DISMISSED || state == STATE_FOCAL_PRESSED) {
                        viewModel.languagesPromptSeen()
                    }
                }
                .show()
    }

    override fun onLogoutEvent() {
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