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

import android.annotation.SuppressLint
import android.view.LayoutInflater
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import io.adventech.blockkit.model.BlockItem
import io.adventech.blockkit.ui.media.MediaPlayer
import io.adventech.blockkit.ui.style.Styler

@Composable
fun VideoContent(blockItem: BlockItem.Video, modifier: Modifier = Modifier) {
    val textStyle = Styler.textStyle(null)
    MediaPlayer(
        source = blockItem.src,
        modifier = modifier,
    ) { exoplayer, _, _, _, _ ->

        PlayerContent(
            exoPlayer = exoplayer,
            modifier = Modifier,
        )

        blockItem.caption?.let {
            Text(
                text = it,
                modifier = Modifier.fillMaxWidth(),
                style = textStyle.copy(
                    fontStyle = FontStyle.Italic,
                    color = textStyle.color.copy(alpha = 0.7f),
                ),
                textAlign = TextAlign.Center
            )
        }

    }
}

@SuppressLint("InflateParams")
@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
private fun PlayerContent(
    exoPlayer: ExoPlayer,
    modifier: Modifier = Modifier,
) {
    val hazeState = remember { HazeState() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16 / 9f)
            .clip(Styler.roundedShape()),
    ) {
        AndroidView(
            factory = { ctx ->
                LayoutInflater.from(ctx).inflate(R.layout.exoplayer, null) as PlayerView
            },
            update = { playerView ->
                playerView.player = exoPlayer
            },
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState),
        )

        Spacer(
            Modifier
                .fillMaxSize(0.5f)
                .align(Alignment.Center)
                .clip(MaterialTheme.shapes.large)
                .hazeEffect(hazeState, HazeMaterials.ultraThin()),
        )
    }
}
