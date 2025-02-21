/*
 * Copyright (c) 2023. Adventech <info@adventech.io>
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

package com.cryart.sabbathschool.core.extensions.context

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import coil.imageLoader
import coil.request.ImageRequest
import com.cryart.sabbathschool.core.R
import timber.log.Timber

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

suspend fun Context.fetchBitmap(source: Any?): Bitmap? {
    val request = ImageRequest.Builder(this)
        .data(source)
        .build()

    val drawable = imageLoader.execute(request).drawable

    return (drawable as? BitmapDrawable)?.bitmap
}
