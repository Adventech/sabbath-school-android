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
package com.cryart.sabbathschool.ui.splash

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.cryart.sabbathschool.data.di.ViewModelFactory
import com.cryart.sabbathschool.extensions.arch.observeNonNull
import com.cryart.sabbathschool.misc.SSConstants
import com.cryart.sabbathschool.ui.quarterlies.QuarterliesActivity
import com.cryart.sabbathschool.view.SSLoginActivity
import com.cryart.sabbathschool.view.SSReadingActivity
import dagger.android.support.DaggerAppCompatActivity
import timber.log.Timber
import javax.inject.Inject

class SplashActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: SplashViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.isSignedInLiveData.observeNonNull(this) { signedIn ->
            if (signedIn) {
                launchMain()
            } else {
                launchLoginActivity()
            }
        }

        try {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(1)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun launchLoginActivity() {
        val intent = Intent(this, SSLoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun launchMain() {
        val intent = if (intent.extras?.containsKey(SSConstants.SS_LESSON_INDEX_EXTRA) == true) {
            val ssLessonIndex = intent.extras?.getString(SSConstants.SS_LESSON_INDEX_EXTRA)
            var ssReadIndex: String? = null
            if (intent.extras?.containsKey(SSConstants.SS_READ_INDEX_EXTRA) == true) {
                ssReadIndex = intent.extras?.getString(SSConstants.SS_READ_INDEX_EXTRA)
            }
            Intent(this, SSReadingActivity::class.java).apply {
                putExtra(SSConstants.SS_LESSON_INDEX_EXTRA, ssLessonIndex)
                putExtra(SSConstants.SS_READ_INDEX_EXTRA, ssReadIndex)
            }
        } else {
            Intent(this, QuarterliesActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        }

        startActivity(intent)
        finish()
    }
}