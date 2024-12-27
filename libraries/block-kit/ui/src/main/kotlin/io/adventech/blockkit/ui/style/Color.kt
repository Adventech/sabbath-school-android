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

package io.adventech.blockkit.ui.style

import androidx.compose.ui.graphics.Color

val Color.Companion.Primary: Color
    get() = Color(0xFF005EC7)

val Color.Companion.Primary50: Color
    get() = Color(0xFFF2F7FC)

val Color.Companion.Primary100: Color
    get() = Color(0xFFE5EFF9)

val Color.Companion.Primary200: Color
    get() = Color(0xFFCCDFF4)

val Color.Companion.Primary300: Color
    get() = Color(0xFF99BFE9)

val Color.Companion.Primary400: Color
    get() = Color(0xFF669EDD)

val Color.Companion.Primary500: Color
    get() = Color(0xFF337DD2)

val Color.Companion.Primary600: Color
    get() = Color(0xFF005EC7)

val Color.Companion.Primary700: Color
    get() = Color(0xFF004A9F)

val Color.Companion.Primary800: Color
    get() = Color(0xFF003877)

val Color.Companion.Primary900: Color
    get() = Color(0xFF002550)

val Color.Companion.Primary950: Color
    get() = Color(0xFF001328)

val Color.Companion.Gray20: Color
    get() = Color(0xFFF9FAFB)

val Color.Companion.Gray50: Color
    get() = Color(0xFFECEFF2)

val Color.Companion.Gray100: Color
    get() = Color(0xFFD9DFE6)

val Color.Companion.Gray200: Color
    get() = Color(0xFFB3BFCC)

val Color.Companion.Gray300: Color
    get() = Color(0xFF94A6B8)

val Color.Companion.Gray400: Color
    get() = Color(0xFF758CA3)

val Color.Companion.Gray500: Color
    get() = Color(0xFF668099)

val Color.Companion.Gray600: Color
    get() = Color(0xFF5C738A)

val Color.Companion.Gray700: Color
    get() = Color(0xFF47596B)

val Color.Companion.Gray800: Color
    get() = Color(0xFF33404C)

val Color.Companion.Gray900: Color
    get() = Color(0xFF1F262E)

val Color.Companion.Gray950: Color
    get() = Color(0xFF141A1F)

val Color.Companion.Sepia100: Color
    get() = Color(0xFFFDF4E6)

val Color.Companion.Sepia200: Color
    get() = Color(0xFFE8DAC4)

val Color.Companion.Sepia300: Color
    get() = Color(0xFFE0D0B8)

val Color.Companion.Sepia400: Color
    get() = Color(0xFF3E3634)

fun Color.Companion.parse(colorString: String): Color =
    Color(color = android.graphics.Color.parseColor(colorString))
