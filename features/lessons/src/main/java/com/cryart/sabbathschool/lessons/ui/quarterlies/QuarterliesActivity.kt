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

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.view.isVisible
import app.ss.lessons.data.model.QuarterlyGroup
import com.cryart.sabbathschool.core.extensions.activity.startIntentWithScene
import com.cryart.sabbathschool.core.extensions.arch.observeNonNull
import com.cryart.sabbathschool.core.extensions.coroutines.flow.collectIn
import com.cryart.sabbathschool.core.extensions.view.viewBinding
import com.cryart.sabbathschool.core.misc.SSConstants
import com.cryart.sabbathschool.core.model.Status
import com.cryart.sabbathschool.core.navigation.AppNavigator
import com.cryart.sabbathschool.lessons.R
import com.cryart.sabbathschool.lessons.databinding.SsActivityQuarterliesBinding
import com.cryart.sabbathschool.lessons.databinding.SsPromptAppReBrandingBinding
import com.cryart.sabbathschool.lessons.ui.base.SSBaseActivity
import com.cryart.sabbathschool.lessons.ui.languages.LanguagesListFragment
import com.cryart.sabbathschool.lessons.ui.lessons.SSLessonsActivity
import com.cryart.sabbathschool.lessons.ui.quarterlies.components.GroupedQuarterlies
import com.cryart.sabbathschool.lessons.ui.quarterlies.components.QuarterliesAppbarComponent
import com.cryart.sabbathschool.lessons.ui.quarterlies.components.QuarterlyListCallbacks
import com.cryart.sabbathschool.lessons.ui.quarterlies.components.QuarterlyListComponent
import com.cryart.sabbathschool.lessons.ui.quarterlies.list.QuarterliesListActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@AndroidEntryPoint
class QuarterliesActivity : SSBaseActivity(), QuarterlyListCallbacks {

    @Inject
    lateinit var appNavigator: AppNavigator

    private val viewModel by viewModels<QuarterliesViewModel>()

    private val binding by viewBinding(SsActivityQuarterliesBinding::inflate)

    private val appbarComponent: QuarterliesAppbarComponent by lazy {
        QuarterliesAppbarComponent(this, binding.appBar, appNavigator)
    }
    private val listComponent: QuarterlyListComponent by lazy {
        QuarterlyListComponent(this, binding.ssQuarterliesList, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        collectData()

        viewModel.viewCreated()
    }

    private fun collectData() {
        val stateFlow = viewModel.quarterliesFlow.map { it.status }
        stateFlow.collectIn(this) { status ->
            binding.apply {
                ssQuarterliesErrorState.isVisible = status == Status.ERROR
            }
        }

        appbarComponent.collect(viewModel.photoUrlFlow)

        listComponent.collect(
            viewModel.quarterliesFlow.map { it.data ?: GroupedQuarterlies.Empty }
        )

        viewModel.lastQuarterlyIndexLiveData.observeNonNull(this) { index ->
            val intent = SSLessonsActivity.launchIntent(this, index)
            startIntentWithScene(intent)
        }
        viewModel.appReBrandingFlow
            .collectIn(this) { show ->
                if (show) {
                    showAppReBrandingPrompt()
                }
            }
    }

    private fun showAppReBrandingPrompt() {
        val binding = SsPromptAppReBrandingBinding.inflate(layoutInflater)

        val alertDialog = MaterialAlertDialogBuilder(this)
            .setView(binding.root)
            .create()

        binding.btnClose.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
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

    override fun onReadClick(index: String) {
        val lessonsIntent = SSLessonsActivity.launchIntent(this, index)
        startIntentWithScene(lessonsIntent)
    }

    override fun onSeeAllClick(group: QuarterlyGroup) {
        val intent = QuarterliesListActivity.launchIntent(this, group)
        startIntentWithScene(intent)
    }

    companion object {
        fun launchIntent(
            context: Context,
            launchDefault: Boolean = true
        ): Intent = Intent(
            context, QuarterliesActivity::class.java
        ).apply {
            putExtra(SSConstants.SS_QUARTERLY_SCREEN_LAUNCH_EXTRA, launchDefault)
        }
    }
}
