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

package app.ss.design.compose.widget.placeholder

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import app.ss.design.compose.extensions.modifier.asPlaceholder
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.widget.divider.Divider

/**
 * Placeholder defaults
 */
object PlaceholderDefaults {

    /** Default shape for Placeholders **/
    val shape = RoundedCornerShape(16.dp)
}

/**
 * A list item placeholder composable.
 */
@Composable
fun PlaceholderListItem(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.height(height = 16.dp))

        Spacer(
            modifier = Modifier
                .size(240.dp, height = 14.dp)
                .padding(horizontal = 16.dp)
                .asPlaceholder(true, shape = PlaceholderDefaults.shape)
        )

        Spacer(modifier = Modifier.height(height = 4.dp))

        Spacer(
            modifier = Modifier
                .size(100.dp, height = 8.dp)
                .padding(horizontal = 16.dp)
                .asPlaceholder(true, shape = PlaceholderDefaults.shape)
        )

        Spacer(modifier = Modifier.height(height = 12.dp))

        Divider()
    }
}

@PreviewLightDark
@Composable
private fun PreviewListItem() {
    SsTheme {
        Surface {
            PlaceholderListItem()
        }
    }
}

/**
 * A Quarterly placeholder composable.
 */
@Composable
fun PlaceholderQuarterly(
    modifier: Modifier = Modifier
) {
    Spacer(
        modifier = modifier
            .size(width = 145.dp, height = 210.dp)
            .asPlaceholder(true, shape = PlaceholderDefaults.shape)
    )
}

@PreviewLightDark
@Composable
private fun PreviewQuarterly() {
    SsTheme {
        Surface {
            PlaceholderQuarterly(modifier = Modifier.padding(16.dp))
        }
    }
}
