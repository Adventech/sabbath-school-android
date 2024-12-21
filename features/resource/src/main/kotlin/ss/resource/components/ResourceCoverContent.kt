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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ss.design.compose.extensions.color.parse
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.theme.color.SsColors
import app.ss.design.compose.widget.button.SsButtonDefaults
import app.ss.design.compose.widget.text.ReadMoreText
import app.ss.models.resource.Resource
import kotlinx.collections.immutable.toImmutableList
import app.ss.translations.R as L10n

@Composable
internal fun ColumnScope.CoverContent(
    resource: Resource,
    type: CoverContentType
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

    val description: @Composable () -> Unit = {
        resource.description?.let {
            ReadMoreText(
                text = it,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp
                ),
                color = Color.White,
                readMoreMaxLines = 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SsTheme.dimens.grid_4, vertical = 8.dp)
                    .clickable { }
            )
        } ?: Spacer(Modifier.fillMaxWidth().height(16.dp))
    }

    val readButton: @Composable () -> Unit = {
        if (resource.cta?.hidden != true) {
            CtaButton(
                color = Color.parse(resource.primaryColorDark),
                text = resource.cta?.text,
                alignment = alignment,
            )
        }
    }

    val featuresRow: @Composable () -> Unit = {
        if (resource.features.isNotEmpty()) {
            // Needs color from style
            ResourceFeatures(resource.features.toImmutableList())
        }
    }

    Text(
        text = resource.title,
        style = MaterialTheme.typography.titleLarge.copy(
            fontSize = 30.sp
        ),
        color = Color.White, // Needs color from style
        textAlign = textAlign,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SsTheme.dimens.grid_4)
            .padding(top = paddingTop, bottom = 8.dp)
    )

    resource.subtitle?.let {
        Text(
            text = it,
            style = MaterialTheme.typography.titleSmall.copy(
                fontSize = 13.sp
            ),
            color = SsColors.White70, // Needs color from style
            textAlign = textAlign,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SsTheme.dimens.grid_4)
                .padding(top = 8.dp, bottom = 16.dp)
        )
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
            onClick = {},
            modifier = Modifier,
            shape = readButtonShape,
            colors = buttonColors,
            elevation = ButtonDefaults.elevatedButtonElevation(readButtonElevation),
            contentPadding = PaddingValues(horizontal = 36.dp)
        ) {
            Text(
                text = text ?: stringResource(id = L10n.string.ss_lessons_read).uppercase(),
                style = MaterialTheme.typography.titleMedium.copy(
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
