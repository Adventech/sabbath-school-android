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

package io.adventech.blockkit.ui.image

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowCircleDown
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.slack.circuit.codegen.annotations.CircuitInject
import dagger.hilt.components.SingletonComponent
import io.adventech.blockkit.ui.image.ImagePreviewScreenState.Event
import io.adventech.blockkit.ui.style.LatoFontFamily
import io.adventech.blockkit.ui.style.theme.BlocksPreviewTheme
import me.saket.telephoto.zoomable.OverzoomEffect
import me.saket.telephoto.zoomable.ZoomLimit
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState
import me.saket.telephoto.zoomable.rememberZoomableState

@CircuitInject(ImagePreviewScreen::class, SingletonComponent::class)
@Composable
fun ImagePreviewScreenUi(state: ImagePreviewScreenState, modifier: Modifier = Modifier) {
    val zoomableState = rememberZoomableImageState(rememberZoomableState(zoomSpec = zoomSpec))

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {

        ZoomableAsyncImage(
            model = state.src,
            contentDescription = state.caption,
            modifier = Modifier
                .aspectRatio(state.aspectRatio)
                .zIndex(1f),
            state = zoomableState,
            clipToBounds = false,
        )

        val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

        IconButton(
            onClick = { state.eventSink(Event.Close) },
            modifier = Modifier
                .align(if (isRtl) Alignment.TopEnd else Alignment.TopStart)
                .safeDrawingPadding()
                .zIndex(2f)
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = null,
                tint = Color.White,
            )
        }

        IconButton(
            onClick = {
                state.eventSink(Event.Download)
            },
            modifier = Modifier
                .align(if (isRtl) Alignment.TopStart else Alignment.TopEnd)
                .safeDrawingPadding()
                .zIndex(2f)
        ) {
            Icon(
                imageVector = Icons.Rounded.ArrowCircleDown,
                contentDescription = null,
                tint = Color.White,
            )
        }

        state.caption?.takeUnless { it.isEmpty() }?.let {
            Text(
                text = it,
                modifier = Modifier
                    .safeDrawingPadding()
                    .padding(bottom = 20.dp)
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(16.dp)
                    .zIndex(3f),
                fontSize = 14.sp,
                fontStyle = FontStyle.Italic,
                fontFamily = LatoFontFamily,
                color = Color.White.copy(alpha = 0.7f),
            )
        }
    }
}

private val zoomSpec = ZoomSpec(maximum = ZoomLimit(factor = 4f, overzoomEffect = OverzoomEffect.RubberBanding))

@Preview
@Composable
private fun DialogPreview() {
    BlocksPreviewTheme {
        Surface {
            ImagePreviewScreenUi(
                state = ImagePreviewScreenState(
                    src = "https://images.unsplash.com/photo-1632210000000-0b1b3b3b3b3b",
                    caption = "Lorem ipsum dolor sit amet consectetur adipiscing elit",
                    aspectRatio = 16 / 9f,
                    eventSink = {},
                ),
            )
        }
    }
}
