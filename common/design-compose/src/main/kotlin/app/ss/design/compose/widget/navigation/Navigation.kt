/*
 * Copyright (c) 2022. Adventech <info@adventech.io>
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

package app.ss.design.compose.widget.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Navigation default values.
 */
private object SsNavigationDefaults {
    @Composable
    fun navigationContentColor() = MaterialTheme.colorScheme.onSurfaceVariant
    @Composable
    fun navigationSelectedItemColor() = MaterialTheme.colorScheme.onPrimaryContainer
    @Composable
    fun navigationIndicatorColor() = MaterialTheme.colorScheme.primaryContainer
}

/**
 * A navigation bar composable that wraps Material 3 [NavigationBar].
 *
 * @param modifier the [Modifier] to be applied to this navigation bar
 * @param content the content of this navigation bar, typically 3-5 [SsNavigationBarItem]s
 */
@Composable
fun SsNavigationBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    NavigationBar(
        modifier = modifier,
        contentColor = SsNavigationDefaults.navigationContentColor(),
        tonalElevation = 0.dp,
        content = content
    )
}

/**
 * A navigation bar item composable that wraps Material 3 [NavigationBarItem].
 *
 * @param selected Whether this item is selected.
 * @param onClick Called when this item is clicked.
 * @param icon Icon for this item, typically an [app.ss.design.compose.widget.icon.IconSlot].
 * @param modifier The [Modifier] to be applied to this item.
 * @param selectedIcon The item icon content when selected.
 * @param enabled Controls the enabled state of this item. When `false`, this component will not
 * respond to user input, and it will appear visually disabled and disabled to accessibility services.
 */
@Composable
fun RowScope.SsNavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    selectedIcon: @Composable () -> Unit = icon,
    enabled: Boolean = true,
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = if (selected) selectedIcon else icon,
        modifier = modifier,
        enabled = enabled,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = SsNavigationDefaults.navigationSelectedItemColor(),
            unselectedIconColor = SsNavigationDefaults.navigationContentColor(),
            selectedTextColor = SsNavigationDefaults.navigationSelectedItemColor(),
            unselectedTextColor = SsNavigationDefaults.navigationContentColor(),
            indicatorColor = SsNavigationDefaults.navigationIndicatorColor()
        )
    )
}
