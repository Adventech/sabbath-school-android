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

@file:OptIn(ExperimentalFoundationApi::class)

package com.cryart.sabbathschool.lessons.ui.lessons.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import app.ss.design.compose.extensions.color.parse
import app.ss.design.compose.extensions.isLargeScreen
import app.ss.design.compose.extensions.modifier.thenIf
import app.ss.design.compose.theme.SsColor
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.widget.button.ButtonSpec
import app.ss.design.compose.widget.button.SsButton
import app.ss.design.compose.widget.button.SsButtonColors
import app.ss.design.compose.widget.content.ContentBox
import app.ss.design.compose.widget.image.RemoteImage
import app.ss.models.PublishingInfo
import app.ss.models.SSLesson
import app.ss.models.SSQuarterlyInfo
import com.cryart.sabbathschool.lessons.R
import com.cryart.sabbathschool.lessons.ui.lessons.components.features.QuarterlyFeaturesRow
import com.cryart.sabbathschool.lessons.ui.lessons.components.features.QuarterlyFeaturesSpec
import com.cryart.sabbathschool.lessons.ui.lessons.components.spec.QuarterlyInfoSpec
import com.cryart.sabbathschool.lessons.ui.lessons.components.spec.toSpec
import com.cryart.sabbathschool.lessons.ui.lessons.intro.LessonIntroModel

private enum class QuarterlyInfoType {
    Primary,
    Secondary,
    SecondaryLarge
}

internal fun LazyListScope.quarterlyInfo(
    info: SSQuarterlyInfo,
    publishingInfo: PublishingInfo?,
    scrollOffset: Float,
    onLessonClick: (SSLesson) -> Unit = {},
    onReadMoreClick: (LessonIntroModel) -> Unit = {}
) {
    val quarterly = info.quarterly

    item {
        val spec = info.toSpec(
            readMoreClick = {
                onReadMoreClick(
                    LessonIntroModel(
                        quarterly.title,
                        quarterly.introduction ?: quarterly.description
                    )
                )
            },
        )
        QuarterlyInfo(
            spec = spec.copy(
                readClick = {
                    spec.todayLessonIndex?.let index@{ index ->
                        val lesson = info.lessons.firstOrNull { it.index == index } ?: return@index
                        onLessonClick(lesson)
                    }
                },
            ),
            scrollOffset = scrollOffset,
            modifier = Modifier.animateItemPlacement()
        )
    }

    publishingInfo(
        publishingInfo,
        primaryColorHex = quarterly.color_primary
    )
}

@Composable
private fun QuarterlyInfo(
    spec: QuarterlyInfoSpec,
    modifier: Modifier = Modifier,
    scrollOffset: Float = 0f,
    isLargeScreen: Boolean = isLargeScreen(),
) {

    val type = when {
        spec.splashImage.isNullOrEmpty() -> {
            if (isLargeScreen) {
                QuarterlyInfoType.SecondaryLarge
            } else {
                QuarterlyInfoType.Secondary
            }
        }
        else -> QuarterlyInfoType.Primary
    }

    val content: @Composable ColumnScope.() -> Unit = {
        Content(
            spec = spec,
            type = type,
        )
    }

    CoverBox(
        color = spec.color,
        splashImage = spec.splashImage,
        contentDescription = spec.title,
        scrollOffset = scrollOffset,
        modifier = modifier
    ) {
        when (type) {
            QuarterlyInfoType.Primary -> {
                ContentPrimary(
                    primaryColor = Color.parse(spec.color),
                    primaryDarkColor = Color.parse(spec.colorDark),
                    content = content,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
            QuarterlyInfoType.Secondary -> {
                ContentSecondary(
                    spec = spec,
                    content = content
                )
            }
            QuarterlyInfoType.SecondaryLarge -> {
                ContentSecondaryLarge(
                    spec = spec,
                    content = content
                )
            }
        }
    }
}

@Composable
private fun CoverBox(
    color: String,
    splashImage: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    scrollOffset: Float = 0f,
    content: @Composable BoxScope.() -> Unit,
) {
    val placeholder: @Composable () -> Unit = {
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.parse(color))
        )
    }
    Box(
        modifier = modifier
            .background(Color.parse(color))
            .thenIf(splashImage != null) {
                Modifier.height(540.dp)
            },
    ) {
        ContentBox(
            content = RemoteImage(
                data = splashImage,
                contentDescription = contentDescription,
                loading = placeholder,
                error = placeholder
            ),
            modifier = Modifier
                .graphicsLayer {
                    translationY = scrollOffset * 0.5f
                }
        )

        content()
    }
}

@Composable
private fun ContentPrimary(
    primaryColor: Color,
    primaryDarkColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {

    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color.Transparent,
            Color.Black.copy(0.1f),
            primaryColor,
            primaryDarkColor,
        ),
    )

    ConstraintLayout(modifier = modifier) {
        val container = createRef()

        val guideline = createGuidelineFromTop(0.4f)

        Column(
            modifier = Modifier
                .constrainAs(container) {
                    top.linkTo(guideline, 16.dp)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .background(gradient)
        ) {
            content()
        }
    }
}

@Composable
private fun ContentSecondary(
    spec: QuarterlyInfoSpec,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
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

        ContentBox(
            content = RemoteImage(
                data = spec.cover
            ),
            modifier = Modifier
                .size(width = 145.dp, height = 210.dp)
                .background(Color.parse(spec.color))
                .shadow(12.dp, CoverImageShape)
                .clip(CoverImageShape)
                .align(Alignment.CenterHorizontally)
        )

        content()
    }
}

@Composable
private fun ContentSecondaryLarge(
    spec: QuarterlyInfoSpec,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
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

        ContentBox(
            content = RemoteImage(
                data = spec.cover
            ),
            modifier = Modifier
                .size(width = 195.dp, height = 270.dp)
                .background(Color.parse(spec.color))
                .shadow(12.dp, CoverImageShape)
                .clip(CoverImageShape)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            content()
        }
    }
}

private val CoverImageShape = RoundedCornerShape(6.dp)

@Composable
private fun ColumnScope.Content(
    spec: QuarterlyInfoSpec,
    type: QuarterlyInfoType
) {
    val textAlign: TextAlign
    val alignment: Alignment.Horizontal
    val paddingTop: Dp

    when (type) {
        QuarterlyInfoType.Primary, QuarterlyInfoType.Secondary -> {
            textAlign = TextAlign.Center
            alignment = Alignment.CenterHorizontally
            paddingTop = 16.dp
        }
        QuarterlyInfoType.SecondaryLarge -> {
            textAlign = TextAlign.Start
            alignment = Alignment.Start
            paddingTop = 0.dp
        }
    }

    val description: @Composable () -> Unit = {
        Text(
            text = spec.description,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clickable { spec.readMoreClick() }
        )
    }

    val readButton: @Composable () -> Unit = {
        SsButton(
            spec = ButtonSpec(
                text = stringResource(id = R.string.ss_lessons_read),
                colors = SsButtonColors(
                    containerColor = Color.parse(spec.colorDark),
                ),
                onClick = spec.readClick
            ),
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .align(alignment)
        )
    }

    val featuresRow: @Composable () -> Unit = {
        if (spec.features.isNotEmpty()) {
            QuarterlyFeaturesRow(spec = QuarterlyFeaturesSpec(spec.features))
        }
    }

    Text(
        text = spec.title,
        style = MaterialTheme.typography.headlineMedium,
        color = Color.White,
        textAlign = textAlign,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = paddingTop, bottom = 8.dp)
    )

    Text(
        text = spec.date.uppercase(),
        style = MaterialTheme.typography.titleSmall,
        color = SsColor.White70,
        textAlign = textAlign,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 16.dp)
    )

    if (type == QuarterlyInfoType.SecondaryLarge) {
        description()
        featuresRow()
        readButton()
    } else {
        readButton()
        description()
        featuresRow()
    }
}

@Preview(
    name = "Quarterly Info",
)
@Preview(
    name = "Quarterly Info ~ Large",
    device = Devices.PIXEL_C
)
@Composable
private fun QuarterlyInfoPreview() {
    SsTheme {
        QuarterlyInfo(
            spec = sampleSpec.copy(
                splashImage = null
            ),
        )
    }
}

@Preview(
    name = "Quarterly Info Splash",
)
@Composable
private fun QuarterlyInfoSplashPreview() {
    SsTheme {
        QuarterlyInfo(
            spec = sampleSpec,
            modifier = Modifier
        )
    }
}

private val sampleSpec = QuarterlyInfoSpec(
    title = "Rest In Christ",
    description = "The Lord is my shepherd, I shall not want.",
    date = "October - November - December",
    color = "#ffaa00",
    colorDark = "#4B3521",
    splashImage = "splash",
    cover = "cover",
    features = emptyList()
)
