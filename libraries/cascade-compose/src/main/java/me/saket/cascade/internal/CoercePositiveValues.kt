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

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.PopupPositionProvider

// TODO: this can be removed when https://issuetracker.google.com/issues/265547235 is fixed.
@Immutable
internal data class CoercePositiveValues(
  val delegate: PopupPositionProvider
) : PopupPositionProvider {

  override fun calculatePosition(
    anchorBounds: IntRect,
    windowSize: IntSize,
    layoutDirection: LayoutDirection,
    popupContentSize: IntSize
  ): IntOffset {
    val position = delegate.calculatePosition(
      anchorBounds = anchorBounds,
      windowSize = windowSize,
      layoutDirection = layoutDirection,
      popupContentSize = popupContentSize
    )
    return position.copy(
      x = maxOf(0, position.x),
      y = maxOf(0, position.y)
    )
  }

  companion object {
    internal fun correctMenuBounds(menuBounds: IntRect): IntRect {
      return menuBounds.translate(
        translateX = minOf(0, menuBounds.left) * -1,
        translateY = minOf(0, menuBounds.top) * -1
      )
    }
  }
}
