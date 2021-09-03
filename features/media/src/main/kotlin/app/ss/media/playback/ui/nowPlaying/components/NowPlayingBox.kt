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

package app.ss.media.playback.ui.nowPlaying.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import app.ss.media.playback.model.AudioFile
import com.cryart.design.theme.Body
import com.cryart.design.theme.Dimens
import com.cryart.design.theme.LatoFontFamily
import com.cryart.design.theme.SSTheme
import com.cryart.design.theme.Spacing24

internal enum class BoxState { Collapsed, Expanded }

internal val sampleAudio = AudioFile(
    "",
    title = "Sabbath Rest",
    artist = "Adult Bible Study Guides",
    imageRatio = "portrait"
)

@Composable
internal fun NowPlayingBox(
    modifier: Modifier = Modifier,
    audio: AudioFile,
    boxState: BoxState = BoxState.Expanded
) {
    BoxWithConstraints {
        val marginTop by animateDpAsState(targetValue = if (boxState == BoxState.Expanded) Spacing24 else 0.dp)
        val constraints = decoupledConstraints(boxState, marginTop = marginTop)

        ConstraintLayout(
            constraints,
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.grid_4)
        ) {
            CoverImage(
                audio = audio,
                boxState = boxState,
                modifier = Modifier.layoutId("image")
            )

            NowPlayingColumn(
                audio = audio,
                boxState = boxState,
                modifier = Modifier.layoutId("text")
            )
        }
    }
}

private fun decoupledConstraints(
    boxState: BoxState,
    marginTop: Dp = 0.dp
): ConstraintSet {
    return ConstraintSet {
        val image = createRefFor("image")
        val text = createRefFor("text")

        constrain(image) {
            when (boxState) {
                BoxState.Collapsed -> {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                }
                BoxState.Expanded -> {
                    linkTo(start = parent.start, end = parent.end)
                    top.linkTo(parent.top, margin = marginTop)
                    bottom.linkTo(text.top)
                }
            }
        }
        constrain(text) {
            when (boxState) {
                BoxState.Collapsed -> {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(image.end)
                }
                BoxState.Expanded -> {
                    linkTo(start = parent.start, end = parent.end)
                    top.linkTo(image.bottom)
                }
            }
        }
    }
}

@Preview(
    name = "Box ~ Expanded"
)
@Composable
private fun NowPlayingBoxPreview() {
    SSTheme {
        Surface {
            NowPlayingBox(
                modifier = Modifier.padding(6.dp),
                audio = sampleAudio
            )
        }
    }
}

@Preview(
    name = "Box ~ Collapsed"
)
@Composable
private fun NowPlayingBoxPreviewCollapsed() {
    SSTheme {
        Surface {
            NowPlayingBox(
                modifier = Modifier.padding(6.dp),
                audio = sampleAudio,
                boxState = BoxState.Collapsed
            )
        }
    }
}

@Composable
private fun NowPlayingColumn(
    modifier: Modifier = Modifier,
    audio: AudioFile,
    boxState: BoxState
) {
    val alignment: Alignment.Horizontal = when (boxState) {
        BoxState.Collapsed -> Alignment.Start
        BoxState.Expanded -> Alignment.CenterHorizontally
    }
    val titleStyle = when (boxState) {
        BoxState.Collapsed -> textStyle().copy(
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
        BoxState.Expanded -> textStyle().copy(
            fontSize = 21.sp,
            fontWeight = FontWeight.Black
        )
    }
    val artistStyle = when (boxState) {
        BoxState.Collapsed -> Body.copy(
            fontSize = 14.sp,
        )
        BoxState.Expanded -> Body.copy(
            fontSize = 17.sp,
        )
    }

    Column(
        modifier = modifier
            .padding(Dimens.grid_2),
        horizontalAlignment = alignment
    ) {
        Text(
            text = audio.title,
            style = titleStyle
        )
        Text(
            text = audio.artist,
            style = artistStyle
        )
    }
}

@Composable
private fun textStyle(): TextStyle = TextStyle(
    color = MaterialTheme.colors.onSurface,
    fontFamily = LatoFontFamily,
    fontWeight = FontWeight.Medium
)
