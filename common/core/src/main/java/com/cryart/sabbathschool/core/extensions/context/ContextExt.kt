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

package com.cryart.sabbathschool.core.extensions.context

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import com.cryart.sabbathschool.core.R
import com.cryart.sabbathschool.core.misc.SSColorTheme
import timber.log.Timber

/**
 * Returns current color primary saved in prefs
 */
val Context.colorPrimary get() = Color.parseColor(SSColorTheme.getInstance(this).colorPrimary)

/**
 * Returns a white color tint in dark-mode
 * or the saved [colorPrimary] in light-mode
 */
val Context.colorPrimaryTint
    get() = if (isDarkTheme()) {
        Color.WHITE
    } else {
        colorPrimary
    }

val Context.colorPrimaryDark get() = Color.parseColor(SSColorTheme.getInstance(this).colorPrimaryDark)

fun Context.isDarkTheme(): Boolean {
    return resources.configuration.uiMode and
        Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
}

fun Context.launchWebUrl(url: String): Boolean {
    return launchWebUrl(url.toWebUri())
}

fun Context.launchWebUrl(uri: Uri): Boolean {
    try {
        val intent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .setUrlBarHidingEnabled(true)
            .setStartAnimations(this, R.anim.slide_up, android.R.anim.fade_out)
            .setExitAnimations(this, android.R.anim.fade_in, R.anim.slide_down)
            .build()
        intent.launchUrl(this, uri)
        return true
    } catch (ignored: Exception) {
        Timber.e(ignored)
    }

    // Fall back to launching a default web browser intent.
    try {
        val intent = Intent(Intent.ACTION_VIEW, uri)
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
            return true
        }
    } catch (ignored: Exception) {
        Timber.e(ignored)
    }

    // We were unable to show the web page.
    return false
}

fun String.toWebUri(): Uri {
    return (if (startsWith("http://") || startsWith("https://")) this else "https://$this").toUri()
}

object ContextHelper {
    @JvmStatic
    fun launchWebUrl(context: Context, url: String) {
        context.launchWebUrl(url)
    }

    @JvmStatic
    fun isDarkTheme(context: Context): Boolean = context.isDarkTheme()
}
