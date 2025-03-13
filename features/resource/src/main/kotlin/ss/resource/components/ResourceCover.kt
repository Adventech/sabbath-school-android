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

package ss.resource.components

import android.content.res.Configuration
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import app.ss.design.compose.extensions.color.parse
import app.ss.design.compose.extensions.isLargeScreen
import app.ss.design.compose.extensions.modifier.thenIf
import app.ss.design.compose.widget.content.ContentBox
import app.ss.design.compose.widget.image.RemoteImage
import coil.size.Scale
import io.adventech.blockkit.model.resource.Resource
import io.adventech.blockkit.model.resource.ResourceCoverType
import io.adventech.blockkit.model.resource.ResourcePreferredCover

@Composable
fun ResourceCover(
    resource: Resource,
    modifier: Modifier = Modifier,
    scrollOffset: () -> Float,
    content: @Composable ColumnScope.(CoverContentType) -> Unit
) {
    val isLargeScreen = isLargeScreen()
    val coverContentType by remember(resource) {
        mutableStateOf(
            if (resource.covers.splash.isNullOrEmpty()) {
                if (isLargeScreen) {
                    CoverContentType.SECONDARY_LARGE
                } else {
                    CoverContentType.SECONDARY
                }
            } else {
                CoverContentType.PRIMARY
            }
        )
    }
    CoverBox(
        color = resource.primaryColor,
        splashImage = resource.covers.splash,
        modifier = modifier,
        scrollOffset = scrollOffset
    ) {
        when (coverContentType) {
            CoverContentType.PRIMARY -> {
                ContentPrimary(
                    splashImage = resource.covers.splash,
                    primaryColor = resource.primaryColor,
                    primaryDarkColor = resource.primaryColorDark,
                    modifier = Modifier.align(Alignment.BottomCenter),
                ) {
                    content(coverContentType)
                }
            }

            CoverContentType.SECONDARY -> {
                ContentSecondary(resource) { content(coverContentType) }
            }

            CoverContentType.SECONDARY_LARGE -> {
                ContentSecondaryLarge(resource) { content(coverContentType) }
            }
        }
    }
}

@Composable
private fun CoverBox(
    color: String,
    splashImage: String?,
    modifier: Modifier = Modifier,
    scrollOffset: () -> Float = { 0f },
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .background(Color.parse(color))
            .thenIf(splashImage != null) {
                Modifier.height(defaultHeight())
            }
    ) {
        CoverImageBox(
            cover = splashImage,
            color = color,
            modifier = Modifier
                .graphicsLayer {
                    translationY = scrollOffset() * 0.5f
                }
        )

        content()
    }
}

@Composable
private fun ContentPrimary(
    splashImage: String?,
    primaryColor: String,
    primaryDarkColor: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(modifier = modifier) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            CoverImageBox(
                cover = splashImage,
                color = primaryColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.4f)
                    .align(Alignment.BottomCenter)
                    .blur(
                        radius = 40.dp,
                        edgeTreatment = BlurredEdgeTreatment.Unbounded,
                    )
            )

            Column(
                modifier = Modifier,
                content = content,
            )
        } else {
            val gradient = remember(primaryColor) {
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(0.1f),
                        Color.parse(primaryColor),
                        Color.parse(primaryDarkColor),
                    )
                )
            }

            Column(
                modifier = Modifier.background(gradient),
                content = content,
            )
        }
    }
}

@Composable
private fun CoverImageBox(
    cover: String?,
    color: String,
    modifier: Modifier = Modifier,
) {
    val placeholder: @Composable () -> Unit = {
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.parse(color))
        )
    }
    ContentBox(
        content = RemoteImage(
            data = cover,
            contentDescription = null,
            scale = Scale.FILL,
            loading = placeholder,
            error = placeholder,
        ),
        modifier = modifier
    )
}

@Composable
private fun ContentSecondary(
    resource: Resource,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val coverType by remember(resource) {
        mutableStateOf(
            when (resource.preferredCover) {
                ResourcePreferredCover.LANDSCAPE -> ResourceCoverType.LANDSCAPE
                ResourcePreferredCover.SQUARE -> ResourceCoverType.SQUARE
                else -> ResourceCoverType.PORTRAIT
            }
        )
    }
    val cover by remember(resource) {
        mutableStateOf(
            when (resource.preferredCover) {
                ResourcePreferredCover.LANDSCAPE -> resource.covers.landscape
                ResourcePreferredCover.SQUARE -> resource.covers.square
                else -> resource.covers.portrait
            }
        )
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                top = with(LocalDensity.current) {
                    WindowInsets.safeDrawing
                        .only(WindowInsetsSides.Top)
                        .getTop(this)
                        .toDp()
                }
            )
    ) {
        Spacer(modifier = Modifier.height(64.dp))

        val placeholder: @Composable () -> Unit = {
            Spacer(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.parse(resource.primaryColorDark), CoverImageShape)
            )
        }

        ContentBox(
            content = RemoteImage(
                data = cover,
                loading = placeholder,
                error = placeholder
            ),
            modifier = Modifier
                .size(nonSplashCover(coverType))
                .background(Color.parse(resource.primaryColorDark), CoverImageShape)
                .shadow(12.dp, CoverImageShape)
                .clip(CoverImageShape)
                .align(Alignment.CenterHorizontally)
        )

        content()
    }
}

@Composable
private fun ContentSecondaryLarge(
    resource: Resource,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val coverType by remember(resource) {
        mutableStateOf(
            when (resource.preferredCover) {
                ResourcePreferredCover.LANDSCAPE -> ResourceCoverType.LANDSCAPE
                ResourcePreferredCover.SQUARE -> ResourceCoverType.SQUARE
                else -> ResourceCoverType.PORTRAIT
            }
        )
    }
    val cover by remember(resource) {
        mutableStateOf(
            when (resource.preferredCover) {
                ResourcePreferredCover.LANDSCAPE -> resource.covers.landscape
                ResourcePreferredCover.SQUARE -> resource.covers.square
                else -> resource.covers.portrait
            }
        )
    }
    val insetsPadding = with(LocalDensity.current) {
        WindowInsets.safeDrawing
            .only(WindowInsetsSides.Top)
            .getTop(this)
            .toDp()
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = insetsPadding + 64.dp, bottom = 32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(32.dp))

        val placeholder: @Composable () -> Unit = {
            Spacer(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.parse(resource.primaryColorDark), CoverImageShape)
            )
        }

        ContentBox(
            content = RemoteImage(
                data = cover,
                loading = placeholder,
                error = placeholder
            ),
            modifier = Modifier
                .size(nonSplashCover(coverType))
                .background(Color.parse(resource.primaryColorDark), CoverImageShape)
                .shadow(12.dp, CoverImageShape)
                .clip(CoverImageShape)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            content()
        }
    }
}

private val CoverImageShape = RoundedCornerShape(6.dp)
private const val MAX_WIDTH = 200f

@Stable
@Composable
private fun nonSplashCover(coverType: ResourceCoverType): DpSize {
    val screenWidth = LocalConfiguration.current.screenWidthDp.toFloat()

    val width = if (isLargeScreen()) MAX_WIDTH else (screenWidth / (if (coverType == ResourceCoverType.PORTRAIT) 2.5f else 1.5f))
    val height = width / coverType.aspectRatio

    return DpSize(width.dp, height.dp)
}

@Stable
@Composable
private fun defaultHeight(): Dp {
    val config = LocalConfiguration.current
    val screenHeight = config.screenHeightDp
    return (screenHeight.toFloat() * (if (isLargeScreen() && (config.orientation == Configuration.ORIENTATION_LANDSCAPE)) 0.8f else 0.6f)).dp
}
