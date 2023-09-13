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
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package app.ss.media.playback.ui.nowPlaying.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ss.design.compose.theme.Dimens
import app.ss.design.compose.theme.LatoFontFamily
import app.ss.design.compose.theme.SsTheme
import app.ss.media.playback.ui.spec.NowPlayingSpec
import app.ss.models.media.AudioFile

internal enum class BoxState { Collapsed, Expanded }

internal val sampleAudio = AudioFile(
    "",
    title = "Sabbath Rest",
    artist = "Adult Bible Study Guides",
    imageRatio = "portrait"
)

@Composable
internal fun NowPlayingColumn(
    spec: NowPlayingSpec,
    boxState: BoxState,
    modifier: Modifier = Modifier
) {
    val horizontalAlignment: Alignment.Horizontal
    val titleStyle: TextStyle
    val artistStyle: TextStyle
    val spacing: Dp
    val textAlign: TextAlign

    when (boxState) {
        BoxState.Collapsed -> {
            horizontalAlignment = Alignment.Start
            titleStyle = textStyle().copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            artistStyle = SsTheme.typography.bodySmall.copy(
                fontSize = 14.sp
            )
            textAlign = TextAlign.Start
            spacing = 2.dp
        }
        BoxState.Expanded -> {
            horizontalAlignment = Alignment.CenterHorizontally
            titleStyle = textStyle().copy(
                fontSize = 21.sp,
                fontWeight = FontWeight.Black
            )
            artistStyle = SsTheme.typography.bodySmall.copy(
                fontSize = 17.sp
            )
            textAlign = TextAlign.Center
            spacing = 8.dp
        }
    }

    val animatedSpacing by animateDpAsState(targetValue = spacing, label = "spacing")

    Column(
        modifier = modifier
            .padding(
                horizontal = Dimens.grid_2,
                vertical = 24.dp
            ),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = Arrangement.spacedBy(animatedSpacing)
    ) {
        Text(
            text = spec.title,
            style = titleStyle,
            textAlign = textAlign
        )
        Text(
            text = spec.artist,
            style = artistStyle,
            textAlign = textAlign,
            color = SsTheme.colors.secondaryForeground
        )
    }
}

@Composable
@Stable
private fun textStyle(): TextStyle = TextStyle(
    color = SsTheme.colors.primaryForeground,
    fontFamily = LatoFontFamily,
    fontWeight = FontWeight.Medium
)
