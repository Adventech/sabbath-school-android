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

package ss.document.components.segment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.ss.design.compose.theme.SsTheme
import io.adventech.blockkit.model.resource.Segment
import io.adventech.blockkit.ui.BlockContent
import io.adventech.blockkit.ui.style.LocalReaderStyle
import io.adventech.blockkit.ui.style.background

@Composable
fun SegmentBlockView(
    segment: Segment,
    modifier: Modifier = Modifier,
) {
    val readerStyle = LocalReaderStyle.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(readerStyle.theme.background()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        segment.subtitle?.let {
            Text(
                text = it,
                modifier = Modifier,
                style = SsTheme.typography.titleMedium
            )
        }

        segment.blocks.orEmpty().forEach { block ->
            BlockContent(block, Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
        }

        Spacer(Modifier.fillMaxWidth().height(SsTheme.dimens.grid_4))
    }
}
