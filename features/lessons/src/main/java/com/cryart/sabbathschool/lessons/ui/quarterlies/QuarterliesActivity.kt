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
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.cryart.sabbathschool.core.extensions.arch.observeNonNull
import com.cryart.sabbathschool.core.extensions.context.colorPrimary
import com.cryart.sabbathschool.core.extensions.coroutines.flow.collectIn
import com.cryart.sabbathschool.core.extensions.view.viewBinding
import com.cryart.sabbathschool.core.misc.SSConstants
import com.cryart.sabbathschool.core.model.ViewState
import com.cryart.sabbathschool.core.navigation.AppNavigator
import com.cryart.sabbathschool.lessons.R
import com.cryart.sabbathschool.lessons.databinding.SsActivityQuarterliesBinding
import com.cryart.sabbathschool.lessons.databinding.SsPromptAppReBrandingBinding
import com.cryart.sabbathschool.lessons.ui.base.SSBaseActivity
import com.cryart.sabbathschool.lessons.ui.languages.LanguagesListFragment
import com.cryart.sabbathschool.lessons.ui.lessons.SSLessonsActivity
import com.cryart.sabbathschool.lessons.ui.quarterlies.components.GroupedQuarterlies
import com.cryart.sabbathschool.lessons.ui.quarterlies.components.QuarterliesAppbarComponent
import com.cryart.sabbathschool.lessons.ui.quarterlies.components.QuarterlyListComponent
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.map
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt.STATE_DISMISSED
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt.STATE_FOCAL_PRESSED
import javax.inject.Inject

@AndroidEntryPoint
class QuarterliesActivity : SSBaseActivity() {

    @Inject
    lateinit var appNavigator: AppNavigator

    private val viewModel by viewModels<QuarterliesViewModel>()

    private val binding by viewBinding(SsActivityQuarterliesBinding::inflate)

    private val appbarComponent: QuarterliesAppbarComponent by lazy {
        QuarterliesAppbarComponent(this, binding.appBar, appNavigator)
    }
    private val listComponent: QuarterlyListComponent by lazy {
        QuarterlyListComponent(this, binding.ssQuarterliesList)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        viewModel.viewStateLiveData.observeNonNull(this) { state ->
            binding.apply {
                ssQuarterliesProgressBar.isVisible = state == ViewState.Loading
                ssQuarterliesErrorState.isVisible = state is ViewState.Error
            }
        }

        appbarComponent.collect(viewModel.photoUrlFlow)

        listComponent.collect(
            viewModel.quarterliesFlow.map { it.data ?: GroupedQuarterlies.Empty }
        )

        viewModel.showLanguagePromptLiveData.observe(this, { showLanguagesPrompt() })
        viewModel.lastQuarterlyIndexLiveData.observeNonNull(this) { index ->
            val intent = SSLessonsActivity.launchIntent(this, index)
            startActivity(intent)
        }
        viewModel.appReBrandingFlow
            .collectIn(this) { show ->
                if (show) {
                    showAppReBrandingPrompt()
                }
            }

        viewModel.viewCreated()
    }

    private fun showLanguagesPrompt() {
        MaterialTapTargetPrompt.Builder(this)
            .setTarget(R.id.ss_quarterlies_menu_filter)
            .setPrimaryText(getString(R.string.ss_quarterlies_filter_languages_prompt_title))
            .setCaptureTouchEventOutsidePrompt(true)
            .setIconDrawableColourFilter(this.colorPrimary)
            .setIconDrawable(ContextCompat.getDrawable(this, R.drawable.ic_translate))
            .setBackgroundColour(this.colorPrimary)
            .setSecondaryText(R.string.ss_quarterlies_filter_languages_prompt_description)
            .setPromptStateChangeListener { _, state ->
                if (state == STATE_DISMISSED || state == STATE_FOCAL_PRESSED) {
                    viewModel.languagesPromptSeen()
                }
            }
            .show()
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
