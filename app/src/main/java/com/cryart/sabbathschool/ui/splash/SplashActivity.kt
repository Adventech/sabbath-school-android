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

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.TaskStackBuilder
import com.cryart.sabbathschool.core.navigation.AppNavigator
import com.cryart.sabbathschool.lessons.ui.lessons.SSLessonsActivity
import com.cryart.sabbathschool.ui.home.HomeActivity
import dagger.hilt.android.AndroidEntryPoint
import ss.foundation.coroutines.flow.collectIn
import ss.libraries.circuit.navigation.LoginScreen
import ss.libraries.circuit.navigation.QuarterliesScreen
import javax.inject.Inject

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private val viewModel: SplashViewModel by viewModels()

    @Inject
    lateinit var appNavigator: AppNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.launchStateFlow.collectIn(this) { state -> handleState(state) }

        viewModel.launch()
    }

    private fun handleState(state: LaunchState) {
        when (state) {
            is LaunchState.Lessons -> {
                with(TaskStackBuilder.create(this@SplashActivity)) {
                    addNextIntent(
                        appNavigator.screenIntent(this@SplashActivity, QuarterliesScreen)
                    )
                    addNextIntentWithParentStack(
                        SSLessonsActivity.launchIntent(this@SplashActivity, state.index)
                    )
                    startActivities()
                }
            }
            LaunchState.Login -> appNavigator.navigate(this, LoginScreen)
            LaunchState.Quarterlies -> appNavigator.navigate(this, QuarterliesScreen)
            LaunchState.Loading -> return
            LaunchState.Home -> startActivity(Intent(this, HomeActivity::class.java), null)
        }

        finish()
    }
}
