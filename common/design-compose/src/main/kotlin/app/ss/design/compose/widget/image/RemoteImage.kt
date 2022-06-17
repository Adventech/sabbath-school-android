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

package app.ss.design.compose.widget.image

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.ss.design.compose.widget.content.ContentSlot
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale

data class RemoteImage(
    val data: String?,
    @DrawableRes val errorRes: Int,
    @DrawableRes val placeholderRes: Int = errorRes,
    val contentDescription: String? = null,
    val cornerRadius: Float = 0f,
    val contentScale: ContentScale = ContentScale.Crop,
    val shape: Shape = RoundedCornerShape(cornerRadius),
    val scale: Scale = Scale.FIT,
    val elevation: Dp = 0.dp,
) : ContentSlot {

    @Composable
    override fun Content() {
        Surface(
            modifier = Modifier,
            color = MaterialTheme.colorScheme.inverseOnSurface,
            tonalElevation = elevation,
            shadowElevation = elevation,
            shape = shape,
        ) {
            val builder = ImageRequest.Builder(LocalContext.current)
                .data(data)
                .crossfade(true)
                .scale(scale = scale)
                .placeholder(placeholderRes)
                .error(errorRes)

            AsyncImage(
                model = builder.build(),
                contentDescription = contentDescription,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape),
                contentScale = contentScale
            )
        }
    }
}
