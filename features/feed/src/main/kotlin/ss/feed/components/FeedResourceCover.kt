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

package ss.feed.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import app.ss.design.compose.extensions.color.parse
import app.ss.design.compose.extensions.modifier.asPlaceholder
import app.ss.design.compose.theme.Dimens
import app.ss.design.compose.widget.content.ContentBox
import app.ss.design.compose.widget.image.RemoteImage
import app.ss.models.feed.FeedDirection
import app.ss.models.feed.FeedView
import app.ss.models.resource.ResourceCovers
import coil.size.Scale

@Immutable
data class FeedResourceCoverSpec(
    val title: String,
    val direction: FeedDirection,
    val covers: ResourceCovers,
    val view: FeedView,
    val primaryColor: String,
)

@Composable
internal fun FeedResourceCover(
    spec: FeedResourceCoverSpec,
    modifier: Modifier = Modifier,
) {

    val aspectRatio: Float
    val image: String
    val scale: Scale

    when (spec.view) {
        FeedView.UNKNOWN -> return
        FeedView.tile,
        FeedView.banner,
        FeedView.square -> {
            image = spec.covers.square
            aspectRatio = 1f
            scale = Scale.FILL
        }

        FeedView.folio -> {
            when (spec.direction) {
                FeedDirection.UNKNOWN -> return
                FeedDirection.vertical -> {
                    image = spec.covers.portrait
                    aspectRatio = 0.6671875f
                }

                FeedDirection.horizontal -> {
                    image = spec.covers.landscape
                    aspectRatio = 1.777777777777778f
                }
            }
            scale = Scale.FIT
        }
    }

    val placeholder: @Composable () -> Unit = {
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .asPlaceholder(visible = true)
        )
    }



    CoverBox(
        color = Color.parse(spec.primaryColor),
        direction = spec.direction,
        view = spec.view,
        modifier = modifier.padding(4.dp),
    ) {
        ContentBox(
            content = RemoteImage(
                data = image,
                contentDescription = spec.title,
                scale = scale,
                loading = placeholder,
                error = placeholder
            ),
            modifier = Modifier
                 .fillMaxSize()
                .clip(RoundedCornerShape(CoverCornerRadius))
        )
    }

}

@Composable
private fun CoverBox(
    color: Color,
    direction: FeedDirection,
    view: FeedView,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // val COVER_MAX_WIDTH = 210.0.dp
    val screenWidth = LocalConfiguration.current.screenWidthDp
    var width = screenWidth.dp - 40.dp
    var height = 200.0.dp

    width = when (direction) {
        FeedDirection.UNKNOWN -> return
        FeedDirection.vertical -> {
            width * 0.30f
        }

        FeedDirection.horizontal -> {
            if (view == FeedView.banner) width * 0.9f else width * 0.70f
        }
    }

    Box(
        modifier = modifier
            .size(width, height)
            .padding(Dimens.grid_1)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize(),
            color = color,
            shadowElevation = 8.dp,
            shape = RoundedCornerShape(8.dp),
            content = content
        )
    }

}

private val CoverCornerRadius = 6.dp
