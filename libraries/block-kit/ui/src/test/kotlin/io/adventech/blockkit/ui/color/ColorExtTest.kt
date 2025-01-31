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

package io.adventech.blockkit.ui.color

import androidx.compose.ui.graphics.Color
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test

class ColorExtTest {

    @Test
    fun `parse hex color - RRGGBBAA format`() {
        val color = Color.parse("#181818ad")

        val alpha = "ad".toInt(16) / 255f

        color shouldBeEqualTo Color(0xAD181818)
        color.alpha shouldBeEqualTo alpha
    }

    @Test
    fun `parse hex color - RRGGBB format`() {
        val color = Color.parse("#181818")

        color shouldBeEqualTo Color(0xFF181818)
        color.alpha shouldBeEqualTo 1f
    }
}
