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

package ss.document.segment.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import app.ss.design.compose.extensions.modifier.asPlaceholder
import app.ss.design.compose.extensions.window.containerHeight
import app.ss.design.compose.widget.content.ContentBox
import app.ss.design.compose.widget.image.RemoteImage

@Composable
internal fun SegmentCover(
    cover: String?,
    modifier: Modifier = Modifier,
    headerContent: @Composable () -> Unit = { },
) {
    val windowInfo = LocalWindowInfo.current
    val height = windowInfo.containerHeight()
    // Calculate image height (50% of screen)
    val imageHeight = (height * 0.5).dp
    // Calculate gradient height (Image Height / 2.5)
    val gradientHeight = imageHeight / 2.5f

    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
        if (cover == null) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((height * 0.15).dp),
                )
                headerContent()
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(imageHeight)
            ) {
                ContentBox(
                    content = RemoteImage(
                        data = cover,
                        loading = { Box(modifier = Modifier.asPlaceholder(true, shape = RectangleShape)) },
                    ),
                    modifier = Modifier.matchParentSize()
                )

                // Top Gradient (Scrim) for Toolbar Visibility
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(gradientHeight) // Covers top ~40% of the image
                        .align(Alignment.TopCenter)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    // Start Dark at the top (0.4 opacity)
                                    Color.Black.copy(alpha = 0.4f),
                                    // Fade to lighter dark
                                    Color.Black.copy(alpha = 0.15f),
                                    // End completely transparent
                                    Color.Transparent
                                ),
                                startY = 0f,
                                endY = Float.POSITIVE_INFINITY
                            )
                        )
                )
            }

            headerContent()
        }
    }
}
