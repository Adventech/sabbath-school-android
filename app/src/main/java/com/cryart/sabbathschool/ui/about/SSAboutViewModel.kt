/*
 * Copyright (c) 2016 Adventech <info@adventech.io>
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

import android.content.Context
import com.cryart.sabbathschool.BuildConfig
import com.cryart.sabbathschool.core.extensions.context.launchWebUrl
import app.ss.translations.R as L10n

class SSAboutViewModel(private val context: Context) {

    val versionInfo: String get() = context.getString(L10n.string.ss_settings_version_with_param_string, BuildConfig.VERSION_NAME)

    fun onFacebookClick() {
        context.launchWebUrl(context.getString(L10n.string.ss_settings_facebook_url))
    }

    fun onInstagramClick() {
        context.launchWebUrl(context.getString(L10n.string.ss_settings_instagram_url))
    }

    fun onGitHubClick() {
        context.launchWebUrl(context.getString(L10n.string.ss_settings_github_url))
    }

    fun onAdventechIoClick() {
        context.launchWebUrl(context.getString(L10n.string.ss_settings_website_url))
    }
}
