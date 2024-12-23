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

import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.palette.graphics.Palette
import app.ss.design.compose.extensions.modifier.asPlaceholder
import app.ss.design.compose.widget.content.ContentBox
import app.ss.design.compose.widget.image.RemoteImage
import app.ss.models.resource.Segment
import app.ss.models.resource.SegmentType

@Composable
fun SegmentCover(
    cover: String?,
    modifier: Modifier = Modifier,
    headerContent: (@Composable (Color) -> Unit)? = null,
) {
    val height = LocalConfiguration.current.screenHeightDp

    val contentColor = remember { mutableStateOf(Color.Black) }

    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
        if (cover == null) {
            Box(modifier = Modifier
                .fillMaxWidth()
                .height((height * 0.2).dp))

            headerContent?.invoke(contentColor.value)
        } else {
            ContentBox(
                content = RemoteImage(
                    data = cover,
                    loading = { Box(modifier = Modifier.asPlaceholder(true, shape = RectangleShape)) },
                    onSuccess = {
                        (it as? BitmapDrawable)?.bitmap?.let {
                            Palette.from(it).generate { palette ->
                                palette?.getDarkMutedColor(android.graphics.Color.BLACK)?.let { dominantColor ->
                                    contentColor.value = Color(dominantColor)
                                }
                            }
                        }
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height((height * 0.5).dp)
            )

            headerContent?.invoke(contentColor.value)
        }
    }
}

internal fun Segment.hasCover(): Boolean {
    return type == SegmentType.BLOCK && cover != null
}
