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

package app.ss.design.compose.widget.scaffold

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons as MaterialIcons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import app.ss.design.compose.extensions.modifier.thenIf
import app.ss.design.compose.extensions.previews.DevicePreviews
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.widget.appbar.SsTopAppBar
import app.ss.design.compose.widget.appbar.TopAppBarSpec
import app.ss.design.compose.widget.appbar.TopAppBarType
import app.ss.design.compose.widget.icon.IconBox
import app.ss.design.compose.widget.icon.IconButtonSlot
import app.ss.design.compose.widget.icon.Icons

/**
 * Scaffold implements the basic material design visual layout structure.
 *
 * This component provides API to put together several material components to construct your
 * screen, by ensuring proper layout strategy for them and collecting necessary data so these
 * components will work together correctly.
 *
 * @param modifier the [Modifier] to be applied to this scaffold
 * @param topBar top app bar of the screen. Use [SsTopAppBar]
 * @param scrollBehavior a [TopAppBarScrollBehavior] which holds various offset values
 * @param content content of your screen. The lambda receives an [PaddingValues] that should be
 * applied to the content root via Modifier.padding to properly offset top and bottom bars. If
 * you're using Modifier.VerticalScroll, apply this modifier to the child of the scroll, and not on
 * the scroll itself.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SsScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier.thenIf(scrollBehavior != null) {
            Modifier.nestedScroll(scrollBehavior!!.nestedScrollConnection)
        },
        topBar = topBar,
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@DevicePreviews
@Composable
private fun Preview() {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    SsTheme {
        SsScaffold(
            topBar = {
                SsTopAppBar(
                    spec = TopAppBarSpec(
                        TopAppBarType.CenterAligned,
                        actions = listOf(
                            IconButtonSlot(
                                imageVector = MaterialIcons.Rounded.AccountCircle,
                                contentDescription = "Profile",
                                onClick = {}
                            )
                        )
                    ),
                    title = { Text(text = "Title") },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            IconBox(icon = Icons.ArrowBack)
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            },
            scrollBehavior = scrollBehavior
        ) { innerPadding ->

            LazyColumn(
                contentPadding = innerPadding,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val list = (0..50).map { it.toString() }
                items(count = list.size) {
                    Text(
                        text = "Hello world ${list[it]}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { }
                            .padding(
                                horizontal = 16.dp,
                                vertical = 12.dp
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun SystemUiEffect(
    lightStatusBar: Boolean,
    isSystemInDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val localView = LocalView.current
    if (!localView.isInEditMode) {
        DisposableEffect(lightStatusBar) {
            val window = (localView.context as Activity).window
            WindowCompat.getInsetsController(window, localView).isAppearanceLightStatusBars = lightStatusBar
            onDispose { WindowCompat.getInsetsController(window, localView).isAppearanceLightStatusBars = !isSystemInDarkTheme }
        }
    }
}
