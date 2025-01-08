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

package ss.resource.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ss.design.compose.extensions.color.parse
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.theme.color.SsColors
import app.ss.design.compose.widget.button.SsButtonDefaults
import app.ss.design.compose.widget.text.ReadMoreText
import io.adventech.blockkit.model.resource.Resource
import io.adventech.blockkit.ui.MarkdownText
import io.adventech.blockkit.ui.style.Styler
import kotlinx.collections.immutable.toImmutableList
import app.ss.translations.R as L10n

@Composable
internal fun ColumnScope.CoverContent(
    resource: Resource,
    type: CoverContentType,
    ctaOnClick: () -> Unit
) {
    val textAlign: TextAlign
    val alignment: Alignment.Horizontal
    val paddingTop: Dp

    when (type) {
        CoverContentType.PRIMARY, CoverContentType.SECONDARY -> {
            textAlign = TextAlign.Center
            alignment = Alignment.CenterHorizontally
            paddingTop = 16.dp
        }

        CoverContentType.SECONDARY_LARGE -> {
            textAlign = TextAlign.Start
            alignment = Alignment.Start
            paddingTop = 0.dp
        }
    }

    val descriptionStyle = resource.style?.resource?.description?.text
    val descriptionColor = descriptionStyle?.color?.let { Color.parse(it) } ?: Color.White

    val description: @Composable () -> Unit = {
        resource.description?.let {
            resource.markdownDescription?.let {
                MarkdownText(
                    markdownText = it,
                    style = Styler.textStyle(descriptionStyle).copy(
                        fontSize = 15.sp,
                        color = descriptionColor
                    ),
                    color = descriptionColor,
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = SsTheme.dimens.grid_4, vertical = 8.dp)
                )
            }

            if (resource.markdownDescription.isNullOrEmpty()) {
                ReadMoreText(
                    text = it,
                    style = Styler.textStyle(descriptionStyle).copy(
                        fontSize = 15.sp,
                        color = descriptionColor
                    ),
                    color = descriptionColor,
                    readMoreMaxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = SsTheme.dimens.grid_4, vertical = 8.dp)
                        .clickable { }
                )
            }

        } ?: Spacer(
            Modifier
                .fillMaxWidth()
                .height(16.dp))
    }

    val readButton: @Composable () -> Unit = {
        if (resource.cta?.hidden != true) {
            CtaButton(
                onClick = ctaOnClick,
                color = Color.parse(resource.primaryColorDark),
                text = resource.cta?.text,
                alignment = alignment,
            )
        }
    }

    val featuresRow: @Composable () -> Unit = {
        if (resource.features.isNotEmpty()) {
            ResourceFeatures(
                features = resource.features.toImmutableList(),
                tint = descriptionColor,
            )
        } else {
            Spacer(
                Modifier
                    .fillMaxWidth()
                    .height(16.dp)
            )
        }
    }

    val styleTemplate = ResourceStyleTemplate()
    val style = resource.style?.resource?.title?.text
    MarkdownText(
        markdownText = resource.markdownTitle ?: resource.title,
        style = SsTheme.typography.titleLarge.copy(
            fontSize = 30.sp,
        ),
        color = Styler.textColor(style, styleTemplate),
        textAlign = Styler.textAlign(style) ?: textAlign,
        styleTemplate = styleTemplate,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SsTheme.dimens.grid_4)
            .padding(top = paddingTop, bottom = 8.dp)
    )

    resource.markdownSubtitle?.let {
        val subtitleStyle = resource.style?.resource?.subtitle?.text
        val subtitleColor = subtitleStyle?.color?.let { Color.parse(it) } ?: SsColors.White70

        MarkdownText(
            markdownText = it,
            style = Styler.textStyle(subtitleStyle, styleTemplate).copy(
                fontSize = 16.sp,
                color = subtitleColor
            ),
            color = subtitleColor,
            textAlign = Styler.textAlign(subtitleStyle) ?: textAlign,
            styleTemplate = styleTemplate,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SsTheme.dimens.grid_4)
                .padding(top = 4.dp, bottom = 16.dp)
        )
    }

    if (resource.markdownSubtitle == null) {
        resource.subtitle?.let {
            Text(
                text = it,
                style = SsTheme.typography.titleSmall.copy(
                    fontSize = 16.sp
                ),
                color = SsColors.White70,
                textAlign = textAlign,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SsTheme.dimens.grid_4)
                    .padding(top = 8.dp, bottom = 16.dp)
            )
        }
    }

    if (type == CoverContentType.SECONDARY_LARGE) {
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
private fun ColumnScope.CtaButton(
    onClick: () -> Unit,
    color: Color,
    text: String?,
    alignment: Alignment.Horizontal
) {
    val buttonColors = SsButtonDefaults.colors(
        containerColor = color
    )
    Row(
        modifier = Modifier
            .defaultMinSize(minWidth = 140.dp)
            .align(alignment)
            .padding(horizontal = SsTheme.dimens.grid_4),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ElevatedButton(
            onClick = onClick,
            modifier = Modifier,
            shape = readButtonShape,
            colors = buttonColors,
            elevation = ButtonDefaults.elevatedButtonElevation(readButtonElevation),
            contentPadding = PaddingValues(horizontal = 36.dp)
        ) {
            Text(
                text = text ?: stringResource(id = L10n.string.ss_lessons_read).uppercase(),
                style = SsTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

private val readButtonElevation = 4.dp
private val readButtonCornerRadius = 20.dp
private val readButtonShape = RoundedCornerShape(readButtonCornerRadius)

enum class CoverContentType {
    PRIMARY,
    SECONDARY,
    SECONDARY_LARGE
}
