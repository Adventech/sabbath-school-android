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

package me.saket.cascade.internal

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.LayoutDirection.Ltr
import me.saket.cascade.BackStackSnapshot

internal fun AnimatedContentTransitionScope<BackStackSnapshot>.cascadeTransitionSpec(
  layoutDirection: LayoutDirection
): ContentTransform {
  val navigatingForward = targetState.backStackSize > initialState.backStackSize

  val inverseMultiplier = if (layoutDirection == Ltr) 1 else -1
  val initialOffset = { width: Int ->
    inverseMultiplier * if (navigatingForward) width else -width / 4
  }
  val targetOffset = { width: Int ->
    inverseMultiplier * if (navigatingForward) -width / 4 else width
  }

  val duration = 350
  return ContentTransform(
    targetContentEnter = slideInHorizontally(tween(duration), initialOffset),
    initialContentExit = slideOutHorizontally(tween(duration), targetOffset),
    targetContentZIndex = targetState.backStackSize.toFloat(),
    sizeTransform = SizeTransform(sizeAnimationSpec = { _, _ -> tween(duration) })
  )
}
