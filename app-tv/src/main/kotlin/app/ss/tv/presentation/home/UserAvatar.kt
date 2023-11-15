/*
 * Copyright (c) 2023. Adventech <info@adventech.io>
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

package app.ss.tv.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.ToggleableSurfaceDefaults
import app.ss.tv.presentation.theme.BorderWidth

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun UserAvatar(
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = ToggleableSurfaceDefaults.shape(shape = CircleShape),
        border = ToggleableSurfaceDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(
                    width = BorderWidth,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                shape = CircleShape
            ),
            selectedBorder = Border(
                border = BorderStroke(
                    width = BorderWidth,
                    color = MaterialTheme.colorScheme.primary
                ),
                shape = CircleShape
            ),
        ),
        scale = ToggleableSurfaceDefaults.scale(focusedScale = 1f),
        modifier = modifier.size(32.dp),
        checked = selected,
        onCheckedChange = { onClick() }
    ) {
        Icon(
            imageVector = Icons.Rounded.AccountCircle,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
