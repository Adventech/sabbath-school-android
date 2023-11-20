/*
 * Copyright (c) 2023. Adventech <info@adventech.io>
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

package app.ss.tv.presentation.videos.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.CardLayoutDefaults
import androidx.tv.material3.Glow
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.StandardCardLayout
import app.ss.tv.data.model.VideoSpec
import app.ss.tv.presentation.theme.BorderWidth
import app.ss.tv.presentation.theme.FocusedGlowElevation
import app.ss.tv.presentation.theme.PressedGlowElevation
import app.ss.tv.presentation.theme.SSTvTheme
import app.ss.tv.presentation.theme.SsCardShape
import app.ss.tv.presentation.videos.ASPECT_RATIO
import app.ss.tv.presentation.videos.CARD_WIDTH
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun VideoRowItem(
    video: VideoSpec,
    onVideoClick: (VideoSpec) -> Unit,
    modifier: Modifier = Modifier,
) {
    StandardCardLayout(
        modifier = modifier
            .width(CARD_WIDTH.dp),
        title = { /* Thumbnails have titles. */ },
        imageCard = {
            CardLayoutDefaults.ImageCard(
                onClick = { onVideoClick(video) },
                interactionSource = it,
                shape = CardDefaults.shape(SsCardShape),
                scale = CardDefaults.scale(
                    focusedScale = 1.1f
                ),
                border = CardDefaults.border(
                    focusedBorder = Border(
                        border = BorderStroke(
                            width = BorderWidth,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = SsCardShape
                    )
                ),
                glow = CardDefaults.glow(
                    focusedGlow = Glow(
                        elevationColor = MaterialTheme.colorScheme.onSurface,
                        elevation = FocusedGlowElevation
                    ),
                    pressedGlow = Glow(
                        elevationColor = MaterialTheme.colorScheme.onSurface,
                        elevation = PressedGlowElevation
                    )
                )
            ) {
                VideoRowItemImage(
                    video = video,
                    modifier = Modifier.aspectRatio(ASPECT_RATIO),
                )
            }
        },
    )
}

@Composable
private fun VideoRowItemImage(
    video: VideoSpec,
    modifier: Modifier = Modifier,
) {
    Box(contentAlignment = Alignment.CenterStart) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .crossfade(true)
                .data(video.thumbnail)
                .build(),
            modifier = modifier
                .fillMaxWidth()
                .drawWithCache {
                    onDrawWithContent {
                        drawContent()
                    }
                },
            contentDescription = "cover image for ${video.title}",
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
@Preview
private fun Preview() {
    SSTvTheme {
        VideoRowItem(
            video = VideoSpec(
                id = "id",
                title = "Paul and the Ephesians",
                artist = "Hope Sabbath School",
                src = "",
                thumbnail = "image.png"
            ),
            onVideoClick = {},
            modifier = Modifier.padding(20.dp)
        )
    }
}
