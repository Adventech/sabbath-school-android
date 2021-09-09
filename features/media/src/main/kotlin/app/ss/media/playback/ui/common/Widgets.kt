/*
 * Copyright (c) 2021. Adventech <info@adventech.io>
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

package app.ss.media.playback.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import coil.transform.RoundedCornersTransformation
import com.cryart.design.theme.Spacing8
import com.google.accompanist.placeholder.PlaceholderDefaults
import com.google.accompanist.placeholder.material.color

@OptIn(ExperimentalCoilApi::class)
@Composable
fun RemoteImage(
    data: String?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    cornerRadius: Float = 0f,
) {
    Box(
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize(),
            color = PlaceholderDefaults.color(),
            elevation = Spacing8,
            shape = RoundedCornerShape(Spacing8),
        ) {
            Image(
                painter = rememberImagePainter(
                    data = data,
                    builder = {
                        crossfade(true)
                        transformations(RoundedCornersTransformation(cornerRadius))
                    }
                ),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop
            )
        }
    }
}
