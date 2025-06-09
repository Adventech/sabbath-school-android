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
      backStack.removeAt(backStack.lastIndex)
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
