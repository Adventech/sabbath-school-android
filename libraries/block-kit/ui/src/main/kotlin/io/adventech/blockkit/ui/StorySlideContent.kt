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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Scale
import coil.size.Size
import io.adventech.blockkit.model.BlockItem
import io.adventech.blockkit.model.ImageStyleTextAlignment
import io.adventech.blockkit.ui.color.parse
import io.adventech.blockkit.ui.style.StoryStyleTemplate
import io.adventech.blockkit.ui.style.Styler

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun StorySlideContent(
    blockItem: BlockItem.StorySlide,
    modifier: Modifier = Modifier,
) {
    val (screenWidth, screenHeight) = LocalConfiguration.current.run {
        screenWidthDp to screenHeightDp
    }
    val density = LocalDensity.current
    val textStyle = Styler.textStyle(
        blockStyle = blockItem.style?.text,
        template = StoryStyleTemplate
    )
    val pages = splitTextIntoPages(blockItem.markdown, screenWidth.dp, textStyle)

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { pages.size },
    )

    val imageScrollState = rememberScrollState()
    var totalScrollableWidth by remember(pages) { mutableFloatStateOf((screenWidth * pages.size).toFloat()) }

    LaunchedEffect(pagerState.currentPage, pagerState.currentPageOffsetFraction) {
        // Calculate the target scroll position
        val progress = (pagerState.currentPage + pagerState.currentPageOffsetFraction) / (pages.size - 1)
        val maxScroll = totalScrollableWidth
        val targetScroll = progress * maxScroll

        imageScrollState.scrollTo(targetScroll.toInt())
    }

    Box(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(imageScrollState)
        ) {
            StoryImage(
                data = blockItem.image,
                screenHeightInPx = with(density) { screenHeight.dp.toPx() },
                onAvailableWidth = { imageWidthInPx ->
                    totalScrollableWidth = with(density) {
                        imageWidthInPx - screenWidth.dp.toPx()
                    }
                },
            )
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier,
        ) { page ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = blockItem.alignment.toAlignment(),
            ) {
                Text(
                    text = pages[page],
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = blockItem.style?.block?.backgroundColor?.let { Color.parse(it) } ?: Color.Transparent)
                        .windowInsetsPadding(WindowInsets.safeContent)
                        .padding(blockItem.alignment.toPadding()),
                    style = textStyle,
                    textAlign = Styler.textAlign(blockItem.style?.text),
                    minLines = DEFAULT_MAX_LINES,
                )
            }
        }
    }
}

private fun ImageStyleTextAlignment.toAlignment(): Alignment = when (this) {
    ImageStyleTextAlignment.TOP -> Alignment.TopStart
    ImageStyleTextAlignment.BOTTOM -> Alignment.BottomStart
    ImageStyleTextAlignment.UNKNOWN -> Alignment.Center
}

private fun ImageStyleTextAlignment.toPadding(): PaddingValues = when (this) {
    ImageStyleTextAlignment.TOP -> PaddingValues(top = 24.dp)
    ImageStyleTextAlignment.BOTTOM -> PaddingValues(bottom = 16.dp)
    ImageStyleTextAlignment.UNKNOWN -> PaddingValues(0.dp)
}

@Composable
private fun splitTextIntoPages(
    text: String,
    screenWidthDp: Dp,
    textStyle: TextStyle,
    maxLines: Int = DEFAULT_MAX_LINES,
): List<AnnotatedString> {
    val layoutDirection = LocalLayoutDirection.current
    val insetPaddings = WindowInsets.safeContent.asPaddingValues()
    val startPadding = insetPaddings.calculateStartPadding(layoutDirection)
    val endPadding = insetPaddings.calculateEndPadding(layoutDirection)

    val textMeasurer = rememberTextMeasurer()
    val maxWidthInPx = with(LocalDensity.current) { (screenWidthDp - (startPadding + endPadding)).toPx() }
    val pages = mutableListOf<AnnotatedString>()

    val styledText = rememberMarkdownText(text, textStyle, StoryStyleTemplate, textStyle.color)

    val layoutResult = textMeasurer.measure(
        text = styledText,
        style = textStyle,
        constraints = Constraints(maxWidth = maxWidthInPx.toInt()),
        layoutDirection = layoutDirection,
    )

    val lineCount = layoutResult.lineCount
    // If line count is less than max lines, return the text as is
    if (lineCount <= maxLines) {
        pages.add(styledText)
        return pages
    }

    // If line count is more than max lines, split the styledText into pages
    // Each page will be an AnnotatedString with max 3 line count
    var currentLine = 0
    while (currentLine < lineCount) {
        val start = layoutResult.getLineStart(currentLine)
        val end = if (currentLine + maxLines < lineCount) {
            layoutResult.getLineStart(currentLine + maxLines)
        } else {
            styledText.length
        }
        val pageText = styledText.subSequence(start, end)
        pages.add(pageText)
        currentLine += maxLines
    }

    return pages
}

@Composable
internal fun StoryImage(
    data: String,
    screenHeightInPx: Float,
    onAvailableWidth: (Float) -> Unit,
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
            val availableWidth = (realWidth / realHeight) * screenHeightInPx
            onAvailableWidth(availableWidth)
        }
    }

    Image(
        painter = painter,
        contentDescription = null,
        modifier = modifier.fillMaxHeight(),
        contentScale = ContentScale.FillHeight,
    )
}

private const val DEFAULT_MAX_LINES = 3
