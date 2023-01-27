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
@file:OptIn(ExperimentalMaterial3WindowSizeClassApi::class)

package com.cryart.sabbathschool.lessons.ui.lessons

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.ss.design.compose.theme.SsTheme
import app.ss.pdf.PdfReader
import com.cryart.sabbathschool.core.extensions.context.shareContent
import com.cryart.sabbathschool.core.extensions.context.toWebUri
import com.cryart.sabbathschool.core.extensions.coroutines.flow.collectIn
import com.cryart.sabbathschool.core.model.AppConfig
import com.cryart.sabbathschool.core.ui.ShareableScreen
import com.cryart.sabbathschool.core.ui.SlidingActivity
import com.cryart.sabbathschool.lessons.R
import com.cryart.sabbathschool.lessons.navigation.lessonIndexArg
import com.cryart.sabbathschool.lessons.ui.lessons.intro.showLessonIntro
import com.cryart.sabbathschool.lessons.ui.readings.SSReadingActivity
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import hotchemi.android.rate.AppRate
import ss.misc.SSConstants
import javax.inject.Inject

@AndroidEntryPoint
class SSLessonsActivity : SlidingActivity(), ShareableScreen {

    @Inject
    lateinit var pdfReader: PdfReader

    @Inject
    lateinit var appConfig: AppConfig

    private val viewModel by viewModels<LessonsViewModel>()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val systemUiController = rememberSystemUiController()

            SsTheme(
                windowWidthSizeClass = calculateWindowSizeClass(activity = this).widthSizeClass
            ) {
                LessonsScreen(
                    state = state,
                    onNavClick = {
                        finishAfterTransition()
                    },
                    onShareClick = { quarterlyTitle ->
                        shareContent(
                            "$quarterlyTitle\n${getShareWebUri()}",
                            getString(R.string.ss_menu_share_app)
                        )
                    },
                    onLessonClick = { lesson ->
                        if (lesson.pdfOnly) {
                            viewModel.pdfLessonSelected(lesson.index)
                        } else {
                            val ssReadingIntent = SSReadingActivity.launchIntent(this, lesson.index)
                            startActivity(ssReadingIntent)
                        }
                    },
                    onReadMoreClick = {
                        supportFragmentManager.showLessonIntro(it)
                    }
                )

                val navigationBarColor = SsTheme.colors.primaryBackground
                SideEffect {
                    systemUiController.setNavigationBarColor(navigationBarColor)
                }
            }
        }

        if (!appConfig.isDebug) {
            AppRate.with(this).setInstallDays(SSConstants.SS_APP_RATE_INSTALL_DAYS).monitor()
            AppRate.showRateDialogIfMeetsConditions(this)
        }

        collectData()
    }

    private fun collectData() {
        viewModel.selectedPdfsFlow.collectIn(this) { pair ->
            val (index, pdfs) = pair
            if (pdfs.isNotEmpty()) {
                startActivity(pdfReader.launchIntent(pdfs, index))
            }
        }
    }

    override fun getShareWebUri(): Uri {
        return "${getString(R.string.ss_app_share_host)}/${viewModel.quarterlyShareIndex}".toWebUri()
    }

    companion object {

        fun launchIntent(
            context: Context,
            quarterlyIndex: String
        ): Intent = Intent(
            context,
            SSLessonsActivity::class.java
        ).apply {
            putExtra(lessonIndexArg, quarterlyIndex)
        }
    }
}
