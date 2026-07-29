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
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.takeOrElse
import io.adventech.blockkit.model.input.HighlightColor
import java.util.Locale
import org.junit.Assert.assertTrue
import org.junit.Test

class HighlightContrastTest {

    @Test
    fun `highlight text meets WCAG AA in every reader theme`() {
        val failures = buildList {
            readerPalettes.forEach { palette ->
                highlightColors.forEach { highlightColor ->
                    val style = highlightColor.toHighlightSpanStyle()
                    val effectiveBackground = style.background.compositeOver(palette.background)
                    val effectiveForeground = style.color.takeOrElse { palette.foreground }
                    val contrast = contrastRatio(effectiveForeground, effectiveBackground)

                    if (contrast < MinimumContrast) {
                        add("${palette.name}/${highlightColor.name}: ${String.format(Locale.ROOT, "%.2f", contrast)}:1")
                    }
                }
            }
        }

        assertTrue(
            "Expected WCAG AA contrast of at least $MinimumContrast:1, failures:\n${failures.joinToString("\n")}",
            failures.isEmpty(),
        )
    }

    private fun contrastRatio(foreground: Color, background: Color): Float {
        val foregroundLuminance = foreground.luminance()
        val backgroundLuminance = background.luminance()
        val lighter = maxOf(foregroundLuminance, backgroundLuminance)
        val darker = minOf(foregroundLuminance, backgroundLuminance)
        return (lighter + 0.05f) / (darker + 0.05f)
    }

    private data class ReaderPalette(
        val name: String,
        val background: Color,
        val foreground: Color,
    )

    private companion object {
        const val MinimumContrast = 4.5f

        val highlightColors = HighlightColor.entries - HighlightColor.UNKNOWN

        val readerPalettes = listOf(
            ReaderPalette("Light", Color.White, Color.Primary950),
            ReaderPalette("Sepia", Color.Sepia100, Color.Sepia400),
            ReaderPalette("Dark", Color.Black, Color.Gray20),
            ReaderPalette("Auto(light)", Color.White, Color.Primary950),
            ReaderPalette("Auto(dark)", Color.Black, Color.Gray20),
        )
    }
}
