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

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import app.ss.design.compose.widget.content.ContentSlot
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Scale

/**
 * A remote image [ContentSlot]
 *
 * Sample usage:
 * ```kotlin
 *      ContentBox(
 *          content = RemoteImage(
 *              data = "url",
 *              contentDescription = "description",
 *              loading = { },
 *              error = { }
 *          ),
 *          modifier = Modifier.size(40.dp)
 *      )
 * ```
 */
@Immutable
data class RemoteImage(
    val data: String?,
    val contentDescription: String? = null,
    val contentScale: ContentScale = ContentScale.Crop,
    val scale: Scale = Scale.FIT,
    val loading: @Composable () -> Unit = {},
    val error: @Composable () -> Unit = {}
) : ContentSlot {

    @Composable
    override fun Content() {
        val painter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
                .data(data)
                .crossfade(true)
                .scale(scale = scale)
                .build()
        )

        when (painter.state) {
            is AsyncImagePainter.State.Loading -> {
                loading()
            }
            AsyncImagePainter.State.Empty,
            is AsyncImagePainter.State.Error -> {
                error()
            }
            is AsyncImagePainter.State.Success -> {}
        }

        Image(
            painter = painter,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = contentScale
        )
    }
}
