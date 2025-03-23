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

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowCircleDown
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Scale
import coil.size.Size
import com.slack.circuit.codegen.annotations.CircuitInject
import dagger.hilt.components.SingletonComponent
import io.adventech.blockkit.ui.image.ImagePreviewScreenState.Event
import io.adventech.blockkit.ui.style.LatoFontFamily
import io.adventech.blockkit.ui.style.theme.BlocksPreviewTheme
import kotlinx.coroutines.delay
import me.saket.telephoto.flick.FlickToDismiss
import me.saket.telephoto.flick.FlickToDismissState
import me.saket.telephoto.flick.rememberFlickToDismissState
import me.saket.telephoto.zoomable.OverzoomEffect
import me.saket.telephoto.zoomable.ZoomLimit
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.ZoomableContentLocation
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.zoomable
import app.ss.translations.R as L10nR

@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(ImagePreviewScreen::class, SingletonComponent::class)
@Composable
fun ImagePreviewScreenUi(state: ImagePreviewScreenState, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var hasStoragePermission by remember {
        // Storage permission is not required on Android R and above.
        mutableStateOf(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
    }
    val launcher = rememberLauncherForActivityResult(RequestPermission()) { wasGranted ->
        hasStoragePermission = wasGranted

        if (wasGranted) {
            state.eventSink(Event.Download(context))
        }
    }

    val flickState = rememberFlickToDismissState()

    LaunchedEffect(flickState.gestureState) {
        when (val gestureState = flickState.gestureState) {
            is FlickToDismissState.GestureState.Dismissing -> {
                delay(gestureState.animationDuration / 2)
                state.eventSink(Event.Close)
            }
            else -> Unit
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {

        FlickToDismiss(flickState) {
            ZoomableAsyncImage(
                data = state.src,
                contentDescription = state.caption,
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1f),
            )
        }

        TopRowActions(
            onBack = { state.eventSink(Event.Close) },
            onDownload = {
                if (hasStoragePermission) {
                    state.eventSink(Event.Download(context))
                } else {
                    launcher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            },
            modifier = Modifier
                .safeDrawingPadding()
                .displayCutoutPadding()
                .align(Alignment.TopCenter)
                .zIndex(2f),
        )

        ImageCaption(
            caption = state.caption,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(3f),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopRowActions(
    onBack: () -> Unit,
    onDownload: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        title = {},
        modifier = modifier,
        navigationIcon = {
            IconButton(
                onClick = onBack,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(L10nR.string.ss_action_back),
                )
            }
        },
        actions = {
            IconButton(
                onClick = onDownload,
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowCircleDown,
                    contentDescription = stringResource(L10nR.string.ss_action_download),
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            navigationIconContentColor = Color.White,
            actionIconContentColor = Color.White
        )
    )
}

@Composable
private fun ImageCaption(caption: String?, modifier: Modifier = Modifier) {
    caption?.takeUnless { it.isEmpty() }?.let {
        Text(
            text = it,
            modifier = modifier
                .safeDrawingPadding()
                .padding(bottom = 20.dp)
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(16.dp),
            fontSize = 14.sp,
            fontStyle = FontStyle.Italic,
            fontFamily = LatoFontFamily,
            color = Color.White.copy(alpha = 0.7f),
        )
    }
}

@Composable
private fun ZoomableAsyncImage(
    data: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(data)
            .crossfade(true)
            .size(Size.ORIGINAL)
            .scale(scale = Scale.FIT)
            .build()
    )
    val zoomableState = rememberZoomableState(zoomSpec)

    when (painter.state) {
        is AsyncImagePainter.State.Success -> {
            zoomableState.setContentLocation(
                ZoomableContentLocation.scaledInsideAndCenterAligned(painter.intrinsicSize)
            )
        }
        else -> Unit
    }

    Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier.zoomable(zoomableState),
        contentScale = ContentScale.Inside,
    )
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
