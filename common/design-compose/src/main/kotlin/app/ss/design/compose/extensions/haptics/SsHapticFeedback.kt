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

package app.ss.design.compose.extensions.haptics

import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

@Stable
interface SsHapticFeedback {

    /**
     * Performs a haptic feedback for a click action.
     */
    fun performClick()

    /**
     * Performs a haptic feedback for a long press action.
     */
    fun performLongPress()

    /**
     * Performs a haptic feedback for a success action.
     */
    fun performSuccess()

    /**
     * Performs a haptic feedback for an error action.
     */
    fun performError()

    /**
     * Performs a haptic feedback for a screen view action.
     */
    fun performScreenView()

    /**
     * Performs a haptic feedback for a segment switch action.
     */
    fun performSegmentSwitch()

    /**
     * Performs a haptic feedback for a switch action.
     */
    fun performToggleSwitch(on: Boolean)

    /**
     * Performs a haptic feedback for a gesture end action.
     */
    fun performGestureEnd()

}

val LocalSsHapticFeedback =
    staticCompositionLocalOf<SsHapticFeedback> { error("CompositionLocal SsHapticFeedback not present") }

internal class DefaultSsHapticFeedback(
    private val hapticFeedback: HapticFeedback
) : SsHapticFeedback {
    override fun performClick() = hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
    override fun performLongPress() = hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
    override fun performSuccess() = hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
    override fun performError() = hapticFeedback.performHapticFeedback(HapticFeedbackType.Reject)
    override fun performScreenView() = hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
    override fun performSegmentSwitch() = hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
    override fun performToggleSwitch(on: Boolean) = hapticFeedback.performHapticFeedback(if (on) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff)
    override fun performGestureEnd() = hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureEnd)
}
