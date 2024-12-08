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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import app.ss.design.compose.extensions.color.parse
import app.ss.design.compose.extensions.modifier.asPlaceholder
import app.ss.design.compose.theme.Dimens
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.widget.content.ContentBox
import app.ss.design.compose.widget.image.RemoteImage
import app.ss.models.feed.FeedDirection
import app.ss.models.feed.FeedView
import app.ss.models.resource.ResourceCovers
import coil.size.Scale

@Immutable
data class FeedResourceCoverSpec(
    val title: String,
    val type: ResourceCoverType,
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
    val image = remember(spec) {
        when (spec.view) {
            FeedView.UNKNOWN -> null
            FeedView.TILE -> spec.covers.landscape
            FeedView.BANNER -> spec.covers.landscape
            FeedView.SQUARE -> spec.covers.square
            FeedView.FOLIO -> spec.covers.portrait
        }
    }
    val primaryColor = remember(spec) { Color.parse(spec.primaryColor) }

    val placeholder: @Composable () -> Unit = {
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .background(primaryColor, RoundedCornerShape(CoverCornerRadius))
                .asPlaceholder(visible = !LocalInspectionMode.current)
        )
    }

    val initialWidth = LocalConfiguration.current.screenWidthDp.toFloat()

    CoverBox(
        color = primaryColor,
        modifier = modifier
            .size(coverSize(spec.type, spec.direction, spec.view, initialWidth)),
    ) {
        ContentBox(
            content = RemoteImage(
                data = image,
                contentDescription = spec.title,
                scale = Scale.FILL,
                loading = placeholder,
                error = placeholder
            ),
            modifier = Modifier.fillMaxSize()
        )
    }

}

@Composable
private fun CoverBox(
    color: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    ElevatedCard(
        modifier = modifier
            .padding(Dimens.grid_1),
        shape = RoundedCornerShape(CoverCornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = color
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = CoverDefaultElevation
        )
    ) { content() }
}

private val CoverDefaultElevation = 8.dp
private val CoverCornerRadius = 6.dp

private val coverSpec = FeedResourceCoverSpec(
    title = "Title",
    type = ResourceCoverType.LANDSCAPE,
    direction = FeedDirection.HORIZONTAL,
    covers = ResourceCovers(
        square = "https://via.placeholder.com/150",
        portrait = "https://via.placeholder.com/150",
        landscape = "https://via.placeholder.com/150",
        splash = "https://via.placeholder.com/150"
    ),
    view = FeedView.FOLIO,
    primaryColor = "#94BDFD"
)
private val coverSpecs = listOf(
    coverSpec,
    coverSpec.copy(
        type = ResourceCoverType.PORTRAIT,
        view = FeedView.FOLIO,
    ),
    coverSpec.copy(
        type = ResourceCoverType.SQUARE,
        direction = FeedDirection.HORIZONTAL,
        view = FeedView.FOLIO,
    ),
    coverSpec.copy(
        type = ResourceCoverType.SPLASH,
        direction = FeedDirection.HORIZONTAL,
        view = FeedView.FOLIO,
    ),
    coverSpec.copy(
        type = ResourceCoverType.LANDSCAPE,
        direction = FeedDirection.VERTICAL,
        view = FeedView.FOLIO,
    ),
)

@PreviewLightDark
@Composable
private fun CoverPreview() {
    SsTheme {
        Surface {
            LazyColumn {
                items(coverSpecs) { spec ->
                    FeedResourceCover(spec)
                }
            }
        }
    }
}

/**
 * Calculate the size of the cover based on the [coverType], [direction], and [viewType].
 */
private fun coverSize(
    coverType: ResourceCoverType,
    direction: FeedDirection,
    viewType: FeedView,
    initialWidth: Float
): DpSize {
    var width = initialWidth - 40
    var height = 200.0f

    when (direction) {
        FeedDirection.UNKNOWN -> Unit
        FeedDirection.VERTICAL -> {
            when (coverType) {
                ResourceCoverType.LANDSCAPE -> {
                    if (viewType == FeedView.TILE) {
                        width *= 0.35f
                    }
                    height = width / coverType.aspectRatio
                }

                ResourceCoverType.PORTRAIT, ResourceCoverType.SQUARE -> {
                    width *= 0.30f
                }

                ResourceCoverType.SPLASH -> {
                    height = width / coverType.aspectRatio
                }
            }
        }

        FeedDirection.HORIZONTAL -> {
            when (coverType) {
                ResourceCoverType.LANDSCAPE -> {
                    width = if (viewType == FeedView.BANNER) width * 0.9f else width * 0.70f
                }

                ResourceCoverType.PORTRAIT, ResourceCoverType.SQUARE -> {
                    width *= 0.40f
                }

                ResourceCoverType.SPLASH -> {
                    height = width / coverType.aspectRatio
                }
            }
        }
    }

    height = width / coverType.aspectRatio

    return DpSize(width.dp, height.dp)
}
