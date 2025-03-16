/*
 * Copyright (c) 2025. Adventech <info@adventech.io>
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
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
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
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ShareCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import app.ss.models.config.AppConfig
import com.cryart.sabbathschool.R
import com.cryart.sabbathschool.core.extensions.context.launchWebUrl
import dagger.hilt.android.AndroidEntryPoint
import ss.misc.SSConstants
import javax.inject.Inject
import app.ss.translations.R as L10n

@AndroidEntryPoint
class AboutActivity : AppCompatActivity() {

    @Inject
    lateinit var appConfig: AppConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.ss_about_activity)

        bindUi()
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
        setSupportActionBar(findViewById<Toolbar>(R.id.ss_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.scrollView)
        ) { v, insets ->
            val innerPadding = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
                    or WindowInsetsCompat.Type.displayCutout()
            )
            v.setPadding(
                innerPadding.left,
                innerPadding.top,
                innerPadding.right,
                innerPadding.bottom
            )
            insets
        }

        findViewById<View>(R.id.instagram_ib).setOnClickListener {
            launchWebUrl(getString(L10n.string.ss_settings_instagram_url))
        }
        findViewById<View>(R.id.facebook_ib).setOnClickListener {
            launchWebUrl(getString(L10n.string.ss_settings_facebook_url))
        }
        findViewById<View>(R.id.github_ib).setOnClickListener {
            launchWebUrl(getString(L10n.string.ss_settings_github_url))
        }
        findViewById<View>(R.id.about_link).setOnClickListener {
            launchWebUrl(getString(L10n.string.ss_settings_website_url))
        }
        findViewById<TextView>(R.id.version_info).text = getString(L10n.string.ss_settings_version_with_param_string, "${appConfig.version} (${appConfig.versionCode})" )
    }
}
