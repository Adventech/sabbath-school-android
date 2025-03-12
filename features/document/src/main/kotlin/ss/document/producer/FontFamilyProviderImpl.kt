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

package ss.document.producer

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import io.adventech.blockkit.model.resource.ResourceFontAttributes
import io.adventech.blockkit.ui.style.font.FontFamilyProvider
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import ss.resources.api.ResourcesRepository

internal class FontFamilyProviderImpl @Inject constructor(
    private val repository: ResourcesRepository,
) : FontFamilyProvider {

    override fun invoke(name: String, defaultFontFamily: FontFamily): Flow<FontFamily> {
        return repository.fontFile(name)
            .map { model ->
                model?.let {
                    val (file, attributes) = model
                    val (weight, style) = attributes.style()

                    FontFamily(Font(file, weight, style))
                } ?: defaultFontFamily
            }
            .catch { emit(defaultFontFamily) }
    }

    private fun ResourceFontAttributes.style(): Pair<FontWeight, FontStyle> {
        return when (this) {
            ResourceFontAttributes.UNKNOWN -> FontWeight.Normal to FontStyle.Normal
            ResourceFontAttributes.THIN -> FontWeight.Thin to FontStyle.Normal
            ResourceFontAttributes.LIGHT -> FontWeight.Light to FontStyle.Normal
            ResourceFontAttributes.EXTRA_LIGHT -> FontWeight.ExtraLight to FontStyle.Normal
            ResourceFontAttributes.REGULAR -> FontWeight.Normal to FontStyle.Normal
            ResourceFontAttributes.MEDIUM -> FontWeight.Medium to FontStyle.Normal
            ResourceFontAttributes.SEMI_BOLD -> FontWeight.SemiBold to FontStyle.Normal
            ResourceFontAttributes.BOLD -> FontWeight.Bold to FontStyle.Normal
            ResourceFontAttributes.EXTRA_BOLD -> FontWeight.ExtraBold to FontStyle.Normal
            ResourceFontAttributes.BLACK -> FontWeight.Black to FontStyle.Normal
            ResourceFontAttributes.THIN_ITALIC -> FontWeight.Thin to FontStyle.Italic
            ResourceFontAttributes.LIGHT_ITALIC -> FontWeight.Light to FontStyle.Italic
            ResourceFontAttributes.EXTRA_LIGHT_ITALIC -> FontWeight.ExtraLight to FontStyle.Italic
            ResourceFontAttributes.REGULAR_ITALIC -> FontWeight.Normal to FontStyle.Italic
            ResourceFontAttributes.MEDIUM_ITALIC -> FontWeight.Medium to FontStyle.Italic
            ResourceFontAttributes.SEMI_BOLD_ITALIC -> FontWeight.SemiBold to FontStyle.Italic
            ResourceFontAttributes.BOLD_ITALIC -> FontWeight.Bold to FontStyle.Italic
            ResourceFontAttributes.EXTRA_BOLD_ITALIC -> FontWeight.ExtraBold to FontStyle.Italic
            ResourceFontAttributes.BLACK_ITALIC -> FontWeight.Black to FontStyle.Italic
        }
    }
}
