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

package ss.document.producer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import app.ss.design.compose.theme.LatoFontFamily
import com.slack.circuit.retained.produceRetainedState
import io.adventech.blockkit.ui.style.font.FontFamilyProvider
import jakarta.inject.Inject
import ss.resources.api.ResourcesRepository
import io.adventech.blockkit.ui.R as BlockkitR

internal class FontFamilyProviderImpl @Inject constructor(
    private val repository: ResourcesRepository,
) : FontFamilyProvider {

    @Composable
    override fun invoke(name: String): FontFamily {
        val customFontFamily by produceRetainedState<FontFamily?>(null) {
            repository.fontFile(name)
                .collect { file ->
                    value = file?.let {
                        FontFamily(
                            Font(it, FontWeight.Normal),
                            Font(it, FontWeight.Bold),
                            Font(BlockkitR.font.lato_italic, FontWeight.Normal, FontStyle.Italic),
                        )
                    }
                }
        }

        return customFontFamily ?: LatoFontFamily
    }

}
