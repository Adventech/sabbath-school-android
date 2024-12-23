/*
 * Copyright (c) 2024. Adventech <info@adventech.io>
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

package ss.document.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import app.ss.design.compose.widget.appbar.SsTopAppBar
import app.ss.design.compose.widget.appbar.TopAppBarSpec
import app.ss.design.compose.widget.appbar.TopAppBarType
import app.ss.design.compose.widget.icon.IconBox
import app.ss.design.compose.widget.icon.Icons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    collapsed: Boolean = false,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    onNavBack: () -> Unit = {},
) {
    SsTopAppBar(
        spec = TopAppBarSpec(
            topAppBarType = TopAppBarType.Small,
        ),
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = onNavBack) {
                val iconColor by animateColorAsState(
                    targetValue = if (collapsed) LocalContentColor.current else Color.White,
                    label = "icon-color"
                )
                IconBox(
                    icon = Icons.ArrowBack,
                    contentColor = iconColor,
                )
            }
        },
        title = {
            Text(text = title)
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent,
        )
    )
}
