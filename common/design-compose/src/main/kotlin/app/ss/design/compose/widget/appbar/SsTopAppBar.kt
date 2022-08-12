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

package app.ss.design.compose.widget.appbar

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.ss.design.compose.widget.icon.IconBox

/**
 * Top app bars display information and actions at the top of a screen.
 *
 * @param spec the specification for this top app bar
 * @param modifier the [Modifier] to be applied to this top app bar
 * @param scrollBehavior a [TopAppBarScrollBehavior] which holds various offset values
 */
@Composable
fun SsTopAppBar(
    spec: TopAppBarSpec,
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    colors: TopAppBarColors? = null
) {
    val actions: @Composable RowScope.() -> Unit = {
        spec.actions.take(MAX_ACTIONS).forEach { icon ->
            IconBox(icon = icon)
        }
    }

    when (spec.topAppBarType) {
        TopAppBarType.Small -> {
            SmallTopAppBar(
                title = title,
                modifier = modifier,
                navigationIcon = navigationIcon,
                actions = actions,
                colors = colors ?: TopAppBarDefaults.smallTopAppBarColors(),
                scrollBehavior = scrollBehavior
            )
        }
        TopAppBarType.Medium -> {
            MediumTopAppBar(
                title = title,
                modifier = modifier,
                navigationIcon = navigationIcon,
                actions = actions,
                colors = colors ?: TopAppBarDefaults.mediumTopAppBarColors(),
                scrollBehavior = scrollBehavior
            )
        }
        TopAppBarType.CenterAligned -> {
            CenterAlignedTopAppBar(
                title = title,
                modifier = modifier,
                navigationIcon = navigationIcon,
                actions = actions,
                colors = colors ?: TopAppBarDefaults.centerAlignedTopAppBarColors(),
                scrollBehavior = scrollBehavior
            )
        }
        TopAppBarType.Large -> {
            LargeTopAppBar(
                title = title,
                modifier = modifier,
                navigationIcon = navigationIcon,
                actions = actions,
                colors = colors ?: TopAppBarDefaults.largeTopAppBarColors(),
                scrollBehavior = scrollBehavior
            )
        }
    }
}

private const val MAX_ACTIONS = 3
