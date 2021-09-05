/*
 * Copyright (c) 2020. Adventech <info@adventech.io>
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

package com.cryart.sabbathschool.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.TaskStackBuilder
import com.cryart.sabbathschool.core.extensions.arch.observeNonNull
import com.cryart.sabbathschool.core.extensions.prefs.SSPrefs
import com.cryart.sabbathschool.lessons.ui.lessons.SSLessonsActivity
import com.cryart.sabbathschool.lessons.ui.quarterlies.QuarterliesActivity
import com.cryart.sabbathschool.ui.login.LoginActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    @Inject
    lateinit var ssPrefs: SSPrefs

    private val viewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.isSignedInLiveData.observeNonNull(
            this,
            { signedIn ->
                if (signedIn) {
                    launchMain()
                } else {
                    startActivity(Intent(this, LoginActivity::class.java))
                }

                finish()
            }
        )
    }

    private fun launchMain() {
        ssPrefs.getLastQuarterlyIndex()?.let { index ->
            with(TaskStackBuilder.create(this)) {
                addNextIntent(
                    QuarterliesActivity.launchIntent(this@SplashActivity)
                )
                addNextIntentWithParentStack(
                    SSLessonsActivity.launchIntent(this@SplashActivity, index)
                )
                startActivities()
            }
        } ?: run {
            startActivity(QuarterliesActivity.launchIntent(this))
        }
    }
}
