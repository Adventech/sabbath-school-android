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

package com.cryart.sabbathschool.core.extensions.view

import android.view.View
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop

/**
 * A helper method wrapping [ViewCompat.setOnApplyWindowInsetsListener], additionally providing the initial padding
 * and margins of the view.
 *
 * This method does not consume any window insets, allowing any and all children to receive the same insets.
 *
 * This is a `set` listener, so only the last [windowInsetsListener] applied by [doOnApplyWindowInsets] will be ran.
 *
 * This approach was based on [https://medium.com/androiddevelopers/windowinsets-listeners-to-layouts-8f9ccc8fa4d1].
 */
fun View.doOnApplyWindowInsets(
    windowInsetsListener: (
        insetView: View,
        windowInsets: WindowInsetsCompat,
        initialPadding: Insets,
        initialMargins: Insets
    ) -> Unit
) {
    val initialPadding = Insets.of(paddingLeft, paddingTop, paddingRight, paddingBottom)
    val initialMargins = Insets.of(marginLeft, marginTop, marginRight, marginBottom)

    ViewCompat.setOnApplyWindowInsetsListener(this) { insetView, windowInsets ->
        windowInsets.also {
            windowInsetsListener(insetView, windowInsets, initialPadding, initialMargins)
        }
    }

    // Whenever a view is detached and then re-attached to the screen, we need to apply insets again.
    //
    // In particular, it is not enough to apply insets only on the first attach:
    //
    // doOnAttach { requestApplyInsets() }
    //
    // For example, considering the following scenario:
    // - A RecyclerView lays out items while in landscape.
    // - Some items that depend on the insets are laid out, and are then detached because they go off-screen.
    // - The user rotates the device 180 degrees. This is still landscape, so no configuration change occurs, but an
    // inset change _does_ occur.
    // - The detached items are reattached because they come back on-screen.
    //
    // At this point, the insets applied to the view would be out of date, and they wouldn't be updated, since the view
    // was already attached once, and the callback for the new insets caused by the rotation would have already been
    // applied, and skipped updating the detached view.
    addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View) {
            v.requestApplyInsets()
        }

        override fun onViewDetachedFromWindow(v: View) = Unit
    })

    // If the view is already attached, immediately request insets be applied.
    if (isAttachedToWindow) {
        requestApplyInsets()
    }
}
