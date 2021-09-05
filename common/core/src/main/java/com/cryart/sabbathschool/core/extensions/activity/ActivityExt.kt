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

package com.cryart.sabbathschool.core.extensions.activity

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import android.transition.Slide
import android.view.Gravity
import android.view.Window
import androidx.core.view.WindowCompat

fun Activity.setLightStatusBar(light: Boolean) {
    WindowCompat.getInsetsController(window, window.decorView)?.isAppearanceLightStatusBars = light
}

fun Activity.slideEnter() {
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
        with(window) {
            requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
            enterTransition = Slide(Gravity.END)
        }
    }
}

fun Activity.startIntentWithScene(intent: Intent) {
    // This crash is caused by a bug in version 6.x the Android platform, which was fixed in OS versions > 7.0.
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
        startActivity(
            intent,
            ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
        )
    } else {
        startActivity(intent)
    }
}
