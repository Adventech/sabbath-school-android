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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.ss.design.compose.extensions.modifier.asPlaceholder
import app.ss.design.compose.theme.Dimens
import app.ss.design.compose.widget.content.ContentBox
import app.ss.design.compose.widget.image.RemoteImage
import app.ss.media.playback.ui.spec.CoverImageSpec
import coil.size.Scale

private interface Sizes {
    val width: Dp
    val height: Dp
}

private enum class CoverOrientation(val key: String) : Sizes {
    SQUARE("square") {
        override val width: Dp = 276.dp
        override val height: Dp = 276.dp
    },
    PORTRAIT("portrait") {
        override val width: Dp = 187.dp
        override val height: Dp = 276.dp
    };

    companion object {
        private val map = values().associateBy(CoverOrientation::key)

        fun fromKey(type: String) = map[type]
    }
}

@Composable
internal fun CoverImage(
    spec: CoverImageSpec,
    modifier: Modifier = Modifier,
    boxState: BoxState = BoxState.Expanded
) {
    val orientation = CoverOrientation.fromKey(spec.imageRatio) ?: CoverOrientation.PORTRAIT
    val collapsed = boxState == BoxState.Collapsed
    val width = if (collapsed) orientation.width / 2 else orientation.width
    val height = if (collapsed) orientation.height / 2 else orientation.height
    val animatedWidth by animateDpAsState(width)
    val animatedHeight by animateDpAsState(height)
    val scale = when (orientation) {
        CoverOrientation.SQUARE -> Scale.FILL
        CoverOrientation.PORTRAIT -> Scale.FIT
    }

    val placeholder: @Composable () -> Unit = {
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .asPlaceholder(visible = true)
        )
    }

    ContentBox(
        content = RemoteImage(
            data = spec.image,
            contentDescription = spec.title,
            scale = scale,
            loading = placeholder,
            error = placeholder
        ),
        modifier = modifier
            .size(
                width = animatedWidth,
                height = animatedHeight
            )
            .padding(Dimens.grid_1)
            .clip(RoundedCornerShape(CoverCornerRadius))
    )
}

@Composable
internal fun CoverImageStatic(
    spec: CoverImageSpec,
    boxState: BoxState,
    modifier: Modifier = Modifier,
) {

    val orientation = CoverOrientation.fromKey(spec.imageRatio) ?: CoverOrientation.PORTRAIT
    val collapsed = boxState == BoxState.Collapsed
    val width = if (collapsed) orientation.width / 2 else orientation.width
    val height = if (collapsed) orientation.height / 2 else orientation.height
    val scale = when (orientation) {
        CoverOrientation.SQUARE -> Scale.FILL
        CoverOrientation.PORTRAIT -> Scale.FIT
    }

    val placeholder: @Composable () -> Unit = {
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .asPlaceholder(visible = true)
        )
    }

    ContentBox(
        content = RemoteImage(
            data = spec.image,
            contentDescription = spec.title,
            scale = scale,
            loading = placeholder,
            error = placeholder
        ),
        modifier = modifier
            .size(
                width = width,
                height = height
            )
            .padding(Dimens.grid_1)
            .clip(RoundedCornerShape(CoverCornerRadius))
    )
}

private val CoverCornerRadius = 6.dp
