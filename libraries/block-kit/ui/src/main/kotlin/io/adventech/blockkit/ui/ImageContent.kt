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

package io.adventech.blockkit.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Scale
import coil.size.Size
import io.adventech.blockkit.model.BlockItem
import io.adventech.blockkit.ui.style.LocalReaderStyle
import io.adventech.blockkit.ui.style.primaryForeground

@Composable
internal fun ImageContent(blockItem: BlockItem.Image, modifier: Modifier = Modifier) {
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(blockItem.src)
            .crossfade(true)
            .scale(Scale.FILL)
            .size(Size.ORIGINAL)
            .build()
    )

    val readerStyle = LocalReaderStyle.current

    Column(modifier = modifier.fillMaxWidth()) {
        Image(
            painter = painter,
            contentDescription = blockItem.caption,
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small),
            contentScale = ContentScale.Crop,
        )

        blockItem.caption?.let {
            Text(
                text = it,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp, fontStyle = FontStyle.Italic),
                color = readerStyle.theme.primaryForeground().copy(alpha = 0.7f)
            )
        }
    }
}
