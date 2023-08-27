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

package com.cryart.sabbathschool.ui.about

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import com.cryart.sabbathschool.BuildConfig
import com.cryart.sabbathschool.R
import com.cryart.sabbathschool.core.extensions.context.launchWebUrl
import com.cryart.sabbathschool.core.extensions.view.viewBinding
import com.cryart.sabbathschool.databinding.SsAboutActivityBinding
import ss.misc.SSConstants
import ss.misc.SSEvent
import app.ss.translations.R as L10n

class AboutActivity : AppCompatActivity() {

    private val binding by viewBinding(SsAboutActivityBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        bindUi()

        SSEvent.track(this, SSConstants.SS_EVENT_ABOUT_OPEN)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finishAfterTransition()
                true
            }
            R.id.ss_action_share -> {
                val shareIntent = ShareCompat.IntentBuilder(this)
                    .setType("text/plain")
                    .setText(getString(L10n.string.ss_menu_share_app_text, SSConstants.SS_APP_PLAY_STORE_LINK))
                    .intent
                if (shareIntent.resolveActivity(packageManager) != null) {
                    startActivity(Intent.createChooser(shareIntent, getString(L10n.string.ss_menu_share_app)))
                }
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.ss_menu_share, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun bindUi() {
        setSupportActionBar(binding.ssToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.instagramIb.setOnClickListener {
            launchWebUrl(getString(L10n.string.ss_settings_instagram_url))
        }
        binding.facebookIb.setOnClickListener {
            launchWebUrl(getString(L10n.string.ss_settings_facebook_url))
        }
        binding.githubIb.setOnClickListener {
            launchWebUrl(getString(L10n.string.ss_settings_github_url))
        }
        binding.aboutLink.setOnClickListener {
            launchWebUrl(getString(L10n.string.ss_settings_website_url))
        }
        binding.versionInfo.text = getString(L10n.string.ss_settings_version_with_param_string, BuildConfig.VERSION_NAME)
    }
}
