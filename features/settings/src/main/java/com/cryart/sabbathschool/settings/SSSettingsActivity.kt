/*
 * Copyright (c) 2021. Adventech <info@adventech.io>
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

package com.cryart.sabbathschool.settings

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import app.ss.models.config.AppConfig
import com.cryart.sabbathschool.core.extensions.coroutines.flow.collectIn
import com.cryart.sabbathschool.core.extensions.view.fadeTo
import com.cryart.sabbathschool.core.extensions.view.viewBinding
import com.cryart.sabbathschool.core.navigation.AppNavigator
import com.cryart.sabbathschool.core.navigation.Destination
import com.cryart.sabbathschool.core.ui.SSBaseActivity
import com.cryart.sabbathschool.settings.databinding.SsSettingsActivityBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ss.misc.SSConstants
import ss.misc.SSEvent
import javax.inject.Inject

@AndroidEntryPoint
class SSSettingsActivity : SSBaseActivity() {

    @Inject
    lateinit var appNavigator: AppNavigator

    @Inject
    lateinit var appConfig: AppConfig

    private val binding by viewBinding(SsSettingsActivityBinding::inflate)

    private val viewModel: SettingsViewModel by viewModels()

    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(
            GoogleSignInOptions.DEFAULT_SIGN_IN
        )
            .requestIdToken(appConfig.webClientId)
            .requestEmail()
            .build()
        GoogleSignIn.getClient(this, gso)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.ssToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportFragmentManager.beginTransaction().replace(
            R.id.ss_settings_frame,
            SSSettingsFragment.newInstance()
        ).commit()

        viewModel.viewStateFlow.collectIn(this) { handleState(it) }

        SSEvent.track(this, SSConstants.SS_EVENT_SETTINGS_OPEN)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finishAfterTransition()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun handleState(state: SettingsState) {
        if (!state.authenticated) {
            lifecycleScope.launch {
                googleSignInClient.signOut().await()
                appNavigator.navigate(this@SSSettingsActivity, Destination.LOGIN)
                finish()
            }
        }

        binding.progress.fadeTo(state.showProgress)
    }
}
