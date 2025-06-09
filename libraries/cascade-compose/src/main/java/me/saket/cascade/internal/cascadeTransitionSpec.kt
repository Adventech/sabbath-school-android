/*
 * Copyright 2020 Saket Narayan.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
