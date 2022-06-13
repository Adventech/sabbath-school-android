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
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import app.ss.design.compose.theme.SsTheme
import app.ss.models.QuarterlyGroup
import com.cryart.sabbathschool.core.extensions.coroutines.flow.collectIn
import com.cryart.sabbathschool.core.navigation.AppNavigator
import com.cryart.sabbathschool.core.navigation.Destination
import com.cryart.sabbathschool.core.ui.SSBaseActivity
import com.cryart.sabbathschool.lessons.databinding.SsPromptAppReBrandingBinding
import com.cryart.sabbathschool.lessons.ui.languages.LanguagesListFragment
import com.cryart.sabbathschool.lessons.ui.lessons.SSLessonsActivity
import com.cryart.sabbathschool.lessons.ui.quarterlies.components.QuarterliesGroupCallback
import com.cryart.sabbathschool.lessons.ui.quarterlies.list.QuarterliesListActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class QuarterliesActivity : SSBaseActivity(), QuarterliesGroupCallback {

    @Inject
    lateinit var appNavigator: AppNavigator

    private val viewModel by viewModels<QuarterliesViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SsTheme {
                QuarterliesScreen(
                    viewModel = viewModel,
                    callbacks = this
                )
            }
        }

        collectData()
    }

    private fun collectData() {
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

    override fun onReadClick(index: String) {
        val lessonsIntent = SSLessonsActivity.launchIntent(this, index)
        startActivity(lessonsIntent)
    }

    override fun onSeeAllClick(group: QuarterlyGroup) {
        val intent = QuarterliesListActivity.launchIntent(this, group)
        startActivity(intent)
    }

    override fun profileClick() {
        appNavigator.navigate(this, Destination.ACCOUNT)
    }

    override fun filterLanguages() {
        val fragment = LanguagesListFragment.newInstance {
            viewModel.languageSelected(it)
        }
        fragment.show(supportFragmentManager, fragment.tag)
    }

    companion object {
        fun launchIntent(
            context: Context,
        ): Intent = Intent(
            context, QuarterliesActivity::class.java
        )
    }
}
