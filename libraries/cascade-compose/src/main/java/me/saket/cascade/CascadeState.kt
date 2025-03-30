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

package me.saket.cascade

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember

@Composable
fun rememberCascadeState(): CascadeState {
  return remember { CascadeState() }
}

/**
 * The state of a [CascadeDropdownMenu].
 */
@Stable
class CascadeState internal constructor() {
  private val backStack = mutableStateListOf<CascadeBackStackEntry>()

  fun navigateBack() {
    backStack.removeLast()
  }

  fun resetBackStack() {
    backStack.clear()
  }

  fun canNavigateBack(): Boolean {
    return backStack.isNotEmpty()
  }

  internal fun navigateTo(entry: CascadeBackStackEntry) {
    if (entry != backStack.lastOrNull()) {
      backStack.add(entry)
    }
  }

  internal fun backStackSnapshot(): BackStackSnapshot {
    return BackStackSnapshot(
      topMostEntry = backStack.lastOrNull(),
      backStackSize = backStack.size
    )
  }
}

@Immutable
internal data class CascadeBackStackEntry(
  val header: @Composable CascadeColumnScope.() -> Unit,
  val childrenContent: @Composable CascadeColumnScope.() -> Unit
)

@Immutable
internal data class BackStackSnapshot(
  val topMostEntry: CascadeBackStackEntry?,
  val backStackSize: Int,
) {
  fun hasParentMenu(): Boolean = backStackSize > 0
}
