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

package com.cryart.sabbathschool.lessons.ui.lessons.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ss.design.compose.extensions.color.parse
import app.ss.design.compose.extensions.isLargeScreen
import app.ss.design.compose.extensions.modifier.thenIf
import app.ss.design.compose.extensions.previews.DayNightPreviews
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.theme.color.SsColors
import app.ss.design.compose.widget.button.SsButtonDefaults
import app.ss.design.compose.widget.content.ContentBox
import app.ss.design.compose.widget.icon.IconBox
import app.ss.design.compose.widget.icon.ResIcon
import app.ss.design.compose.widget.image.RemoteImage
import app.ss.design.compose.widget.text.ReadMoreText
import app.ss.models.OfflineState
import com.cryart.sabbathschool.lessons.ui.lessons.components.features.QuarterlyFeaturesRow
import com.cryart.sabbathschool.lessons.ui.lessons.components.features.QuarterlyFeaturesSpec
import com.cryart.sabbathschool.lessons.ui.lessons.components.spec.PublishingInfoSpec
import com.cryart.sabbathschool.lessons.ui.lessons.components.spec.QuarterlyInfoSpec
import app.ss.translations.R as L10n

private enum class QuarterlyInfoType {
    Primary,
    Secondary,
    SecondaryLarge
}

internal fun LazyListScope.quarterlyInfo(
    info: QuarterlyInfoSpec,
    publishingInfo: PublishingInfoSpec?,
    scrollOffset: () -> Float,
    onLessonClick: (LessonItemSpec) -> Unit
) {
    item {
        QuarterlyInfo(
            spec = info.copy(
                readClick = {
                    info.todayLessonIndex?.let index@{ index ->
                        val lesson = info.lessons.firstOrNull { it.index == index } ?: return@index
                        onLessonClick(lesson)
                    }
                }
            ),
            scrollOffset = scrollOffset,
            modifier = Modifier
        )
    }

    publishingInfo(publishingInfo)
}

@Composable
private fun QuarterlyInfo(
    spec: QuarterlyInfoSpec,
    modifier: Modifier = Modifier,
    scrollOffset: () -> Float = { 0f },
    isLargeScreen: Boolean = isLargeScreen()
) {
    val type by remember(spec) {
        mutableStateOf(
            when {
                spec.splashImage.isNullOrEmpty() -> {
                    if (isLargeScreen) {
                        QuarterlyInfoType.SecondaryLarge
                    } else {
                        QuarterlyInfoType.Secondary
                    }
                }

                else -> QuarterlyInfoType.Primary
            }
        )
    }

    val content: @Composable ColumnScope.() -> Unit = {
        Content(
            spec = spec,
            type = type
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
    scrollOffset: () -> Float = { 0f },
    content: @Composable BoxScope.() -> Unit
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
            }
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
                    translationY = scrollOffset() * 0.5f
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
    content: @Composable ColumnScope.() -> Unit
) {
    val gradient = remember(primaryColor.toArgb()) {
        Brush.verticalGradient(
            colors = listOf(
                Color.Transparent,
                Color.Black.copy(0.1f),
                primaryColor,
                primaryDarkColor
            )
        )
    }

    Column(
        modifier = modifier
            .background(gradient)
            .padding(
                top = with(LocalDensity.current) {
                    WindowInsets.safeDrawing
                        .only(WindowInsetsSides.Top)
                        .getTop(this)
                        .toDp()
                }
            ),
        content = content,
    )
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
        ReadMoreText(
            text = spec.description,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 15.sp
            ),
            color = Color.White,
            readMoreMaxLines = 3,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clickable { spec.readMoreClick() }
        )
    }

    val readButton: @Composable () -> Unit = {
        ReadButton(spec = spec, alignment = alignment)
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
        color = SsColors.White70,
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

@Composable
private fun ColumnScope.ReadButton(
    spec: QuarterlyInfoSpec,
    alignment: Alignment.Horizontal
) {
    val buttonColors = SsButtonDefaults.colors(
        containerColor = Color.parse(spec.colorDark)
    )
    val offlineStateIcon by remember(spec.offlineState) {
        mutableStateOf(
            when (spec.offlineState) {
                OfflineState.PARTIAL,
                OfflineState.NONE -> ResIcon.Download

                OfflineState.IN_PROGRESS -> null
                OfflineState.COMPLETE -> ResIcon.Downloaded
            }
        )
    }
    Row(
        modifier = Modifier
            .defaultMinSize(minWidth = 140.dp)
            .align(alignment)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ElevatedButton(
            onClick = spec.readClick,
            modifier = Modifier,
            shape = readButtonShape,
            colors = buttonColors,
            elevation = ButtonDefaults.elevatedButtonElevation(readButtonElevation),
            contentPadding = PaddingValues(horizontal = 30.dp)
        ) {
            Text(
                text = stringResource(id = L10n.string.ss_lessons_read).uppercase(),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Spacer(
            Modifier
                .size(width = 1.dp, height = 48.dp)
                .padding(vertical = 4.dp)
                .background(Color.parse(spec.color)),
        )

        val progressAlpha by animateFloatAsState(
            if (spec.offlineState == OfflineState.IN_PROGRESS) 1f else 0f,
            label = "progress"
        )

        FilledIconButton(
            onClick = spec.offlineStateClick,
            modifier = Modifier
                .graphicsLayer { translationX = -12f },
            containerColor = Color.parse(spec.colorDark),
            contentColor = downloadButtonIconColor
        ) {
            AnimatedContent(
                targetState = offlineStateIcon,
                label = "download-icon"
            ) { targetIcon ->

                Box(modifier = Modifier, Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(4.dp)
                            .alpha(progressAlpha),
                        color = downloadButtonIconColor
                    )

                    targetIcon?.let { IconBox(icon = it) }
                }
            }
        }
    }
}

private val readButtonElevation = 4.dp
private val readButtonCornerRadius = 20.dp
private val readButtonShape = RoundedCornerShape(topStart = readButtonCornerRadius, bottomStart = readButtonCornerRadius)
private val downloadButtonIconColor = Color(0X80FFFFFF)
private val downloadButtonShape = RoundedCornerShape(topEnd = readButtonCornerRadius, bottomEnd = readButtonCornerRadius)

@Composable
private fun FilledIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = downloadButtonShape,
    containerColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit
) = Surface(
    onClick = onClick,
    modifier = modifier.semantics { role = Role.Button },
    enabled = enabled,
    shape = shape,
    color = containerColor,
    contentColor = contentColor,
    shadowElevation = readButtonElevation,
    interactionSource = interactionSource
) {
    Box(
        modifier = Modifier.size(40.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@DayNightPreviews
@Composable
private fun ReadButtonPreview() {
    SsTheme {
        Surface {
            Column {
                ReadButton(
                    spec = sampleSpec,
                    alignment = Alignment.CenterHorizontally
                )
            }
        }
    }
}

private val sampleSpec = QuarterlyInfoSpec(
    title = "Rest In Christ",
    description = "The Lord is my shepherd, I shall not want.",
    date = "October - November - December",
    color = "#ffaa00",
    colorDark = "#4B3521",
    splashImage = "splash",
    offlineState = OfflineState.NONE,
    lessons = emptyList(),
    cover = "cover",
    features = emptyList()
)
