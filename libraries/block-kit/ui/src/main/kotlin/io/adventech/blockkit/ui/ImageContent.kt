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

package io.adventech.blockkit.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Scale
import coil.size.Size
import com.slack.circuit.foundation.CircuitContent
import com.slack.circuit.foundation.NavEvent
import io.adventech.blockkit.model.BlockItem
import io.adventech.blockkit.ui.dialog.FullScreenDialog
import io.adventech.blockkit.ui.image.ImagePreviewScreen
import io.adventech.blockkit.ui.style.LatoFontFamily
import io.adventech.blockkit.ui.style.Styler
import io.adventech.blockkit.ui.style.thenIf

@Composable
internal fun ImageContent(blockItem: BlockItem.Image, modifier: Modifier = Modifier) {
    var showPreview by remember { mutableStateOf(false) }
    var loadedImage by remember { mutableStateOf(false) }
    val shape = Styler.roundedShape()
    var aspectRatio by remember { mutableFloatStateOf(blockItem.style?.image?.aspectRatio ?: (16f / 9f)) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(aspectRatio)
                .thenIf(blockItem.style?.block?.rounded != false) {
                    clip(shape).background(color = Styler.backgroundColor(null), shape)
                }
                .clickable(
                    enabled = blockItem.style?.image?.expandable != false && loadedImage,
                    onClick = { showPreview = true }
                )
        ) {
            AsyncImage(
                data = blockItem.src,
                contentDescription = blockItem.caption,
                onAvailableAspectRatio = {
                    aspectRatio = it
                    loadedImage = true
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        blockItem.caption?.takeUnless { it.isEmpty() }?.let { text ->
            var textAlign by remember(text) { mutableStateOf(TextAlign.Start) }
            Text(
                text = text,
                modifier = Modifier.fillMaxWidth(),
                style = Styler.textStyle(null).copy(
                    fontSize = 14.sp,
                    fontStyle = FontStyle.Italic,
                    fontFamily = LatoFontFamily,
                ),
                color = Styler.textColor(null).copy(alpha = 0.7f),
                textAlign = textAlign,
                onTextLayout = { textLayoutResult ->
                    textAlign = if (textLayoutResult.lineCount > 1) TextAlign.Start else TextAlign.Center
                }
            )
        }
    }

    if (showPreview) {
        ImageContentPreview(
            id = blockItem.id,
            data = blockItem.src,
            contentDescription = blockItem.caption,
            modifier = Modifier,
            onDismiss = { showPreview = false },
        )
    }
}

/** Crop the image while maintaining original image aspect ratio. **/
@Composable
private fun AsyncImage(
    data: String,
    contentDescription: String?,
    onAvailableAspectRatio: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(data)
            .size(Size.ORIGINAL)
            .scale(scale = Scale.FILL)
            .build()
    )

    when (val state = painter.state) {
        is AsyncImagePainter.State.Loading -> Unit
        AsyncImagePainter.State.Empty,
        is AsyncImagePainter.State.Error -> Unit

        is AsyncImagePainter.State.Success -> {
            val (realWidth, realHeight) = state.painter.intrinsicSize.run {
                width to height
            }
            val imageAspectRatio = (realWidth / realHeight)
            onAvailableAspectRatio(imageAspectRatio)
        }
    }

    Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Crop,
    )
}

@Composable
private fun ImageContentPreview(
    id: String,
    data: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
) {
    FullScreenDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        windowLightStatusBar = false,
    ) {
        CircuitContent(
            screen = ImagePreviewScreen(id, data, contentDescription),
            onNavEvent = { navEvent ->
                when (navEvent) {
                    is NavEvent.Pop -> onDismiss()
                    else -> Unit
                }
            },
        )
    }
}


@Composable
internal fun AsyncImageBox(
    data: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    scale: Scale = Scale.FIT,
    loading: @Composable () -> Unit = {},
    error: @Composable () -> Unit = {},
) {
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(data)
            .crossfade(true)
            .scale(scale = scale)
            .build()
    )

    Box(modifier = modifier) {
        when (painter.state) {
            is AsyncImagePainter.State.Loading -> {
                loading()
            }
            AsyncImagePainter.State.Empty,
            is AsyncImagePainter.State.Error -> {
                error()
            }
            is AsyncImagePainter.State.Success -> Unit
        }

        Image(
            painter = painter,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = contentScale,
        )
    }
}
