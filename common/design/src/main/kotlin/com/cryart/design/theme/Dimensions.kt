/*
 * Copyright (c) 2021. Adventech <info@adventech.io>
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

package com.cryart.design.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

class Dimensions(
    val grid_1: Dp,
    val grid_2: Dp,
    val grid_3: Dp,
    val grid_4: Dp,
    val grid_5: Dp,
    val grid_6: Dp,
    val grid_7: Dp,
    val grid_8: Dp,
)

val smallDimensions = Dimensions(
    grid_1 = 4.dp,
    grid_2 = 8.dp,
    grid_3 = 12.dp,
    grid_4 = 16.dp,
    grid_5 = 20.dp,
    grid_6 = 24.dp,
    grid_7 = 28.dp,
    grid_8 = 32.dp,
)

val sw600Dimensions = Dimensions(
    grid_1 = 8.dp,
    grid_2 = 16.dp,
    grid_3 = 24.dp,
    grid_4 = 32.dp,
    grid_5 = 40.dp,
    grid_6 = 48.dp,
    grid_7 = 56.dp,
    grid_8 = 64.dp,
)

@Composable
fun ProvideDimens(
    dimensions: Dimensions,
    content: @Composable () -> Unit
) {
    val dimensionSet = remember { dimensions }
    CompositionLocalProvider(LocalAppDimens provides dimensionSet, content = content)
}

internal val LocalAppDimens = staticCompositionLocalOf {
    smallDimensions
}

val Dimens: Dimensions
    @Composable
    get() = SsTheme.dimens
