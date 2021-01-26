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
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import com.cryart.sabbathschool.R
import com.cryart.sabbathschool.core.misc.SSColorTheme

class SSAboutViewModel(private val context: Context) {

    val colorPrimary: Int get() = Color.parseColor(SSColorTheme.getInstance(context).colorPrimary)

    fun onFacebookClick() {
        onLinkClick(context.getString(R.string.ss_settings_facebook_url))
    }

    fun onInstagramClick() {
        onLinkClick(context.getString(R.string.ss_settings_instagram_url))
    }

    fun onGitHubClick() {
        onLinkClick(context.getString(R.string.ss_settings_github_url))
    }

    fun onAdventechIoClick() {
        onLinkClick(context.getString(R.string.ss_settings_website_url))
    }

    private fun onLinkClick(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }
}
