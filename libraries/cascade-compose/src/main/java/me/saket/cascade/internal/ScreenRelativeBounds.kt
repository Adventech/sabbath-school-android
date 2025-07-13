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

import android.view.View
import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.toSize
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat.Type.displayCutout
import androidx.core.view.WindowInsetsCompat.Type.ime
import androidx.core.view.WindowInsetsCompat.Type.systemBars

@Immutable
internal data class ScreenRelativeBounds(
  val boundsInRoot: Rect,
  val root: RootLayoutCoordinatesInfo,
) {
  val boundsInWindow: Rect
    get() = Rect(
      offset = boundsInRoot.topLeft + root.layoutPositionInWindow,
      size = boundsInRoot.size,
    )
}

@Immutable
internal data class ScreenRelativeOffset(
  val positionInWindow: Offset,
  val root: RootLayoutCoordinatesInfo,
) {
  val positionInRoot: Offset
    get() = positionInWindow - root.layoutPositionInWindow
}

@Immutable
internal data class RootLayoutCoordinatesInfo(
  /**
   * The boundaries of this layout relative to the window's origin.
   */
  val layoutPositionInWindow: Offset,
  /**
   * This can include system bars because PopupPositionProvider
   * does not take insets into account.
   */
  val windowBoundsMinusIme: Rect,
)

internal fun ScreenRelativeBounds(coordinates: LayoutCoordinates, owner: View): ScreenRelativeBounds {
  return ScreenRelativeBounds(
    boundsInRoot = Rect(
      offset = coordinates.positionInRoot(),
      size = coordinates.size.toSize()
    ),
    root = RootLayoutCoordinatesInfo(
      // material3 uses View#getWindowVisibleDisplayFrame() for calculating window size,
      // but that produces infinite-like values for windows that have FLAG_LAYOUT_NO_LIMITS
      // set (source: WindowLayout.java). material3 ends up looking okay because WindowManager
      // sanitizes bad values.
      layoutPositionInWindow = coordinates.findRootCoordinates().positionInWindow(),
      windowBoundsMinusIme = owner.rootView.getBoundsOnScreenAsRoot().let { bounds ->
        // I should probably be using WindowManager#getCurrentWindowMetrics(), but it is
        // ~10x slower and this function may get called many times if the UI is changing.
        val insets = ViewCompat.getRootWindowInsets(owner)?.getInsets(ime()) ?: Insets.NONE
        bounds.copy(
          left = bounds.left + insets.left,
          top = bounds.top + insets.top,
          right = bounds.right - insets.right,
          bottom = bounds.bottom - insets.bottom,
        )
      }
    )
  )
}

private fun View.getBoundsOnScreenAsRoot(): Rect {
  check(this === rootView)
  return Rect(
    offset = intArrayBuffer.let {
      getLocationOnScreen(it)
      Offset(x = it[0].toFloat(), y = it[1].toFloat())
    },
    size = Size(width.toFloat(), height.toFloat()),
  )
}

// I don't expect this to be shared across threads to need any synchronization.
private val intArrayBuffer = IntArray(size = 2)

/**
 * Calculate a position in another window such that its visual location on screen
 * remains unchanged. That is, its offset from screen's 0,0 remains the same.
 * */
internal fun ScreenRelativeOffset.positionInWindowOf(other: ScreenRelativeBounds): Offset {
  return positionInRoot -
    (other.root.layoutPositionInWindow - root.layoutPositionInWindow) -
    (other.root.windowBoundsMinusIme.topLeft - root.windowBoundsMinusIme.topLeft)
}
