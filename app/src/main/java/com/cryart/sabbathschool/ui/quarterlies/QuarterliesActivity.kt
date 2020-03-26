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

import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.cryart.sabbathschool.R
import com.cryart.sabbathschool.adapter.SSQuarterliesAdapter
import com.cryart.sabbathschool.data.di.ViewModelFactory
import com.cryart.sabbathschool.data.model.Status
import com.cryart.sabbathschool.extensions.arch.getViewModel
import com.cryart.sabbathschool.extensions.arch.observeNonNull
import com.cryart.sabbathschool.misc.SSColorTheme
import com.cryart.sabbathschool.view.SSBaseActivity
import dagger.android.AndroidInjection
import javax.inject.Inject

class QuarterliesActivity : SSBaseActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var viewModel: QuarterliesViewModel

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

        viewModel = getViewModel(this, viewModelFactory)
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

    override fun onLogoutEvent() {
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.ss_quarterlies_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.ss_quarterlies_menu_filter) {
            // show languages filter
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}