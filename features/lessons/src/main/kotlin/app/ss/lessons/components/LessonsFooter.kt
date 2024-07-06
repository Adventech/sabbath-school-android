/*
 * Copyright (c) 2023. Adventech <info@adventech.io>
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

package app.ss.lessons.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ss.design.compose.extensions.color.lighter
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.theme.color.SsColors
import app.ss.lessons.components.features.FeatureImage
import app.ss.lessons.components.spec.CreditSpec
import app.ss.lessons.components.spec.FeatureSpec
import kotlinx.collections.immutable.ImmutableList
import java.util.Calendar
import app.ss.translations.R as L10n

internal fun LazyListScope.footer(
    credits: ImmutableList<CreditSpec>,
    features: ImmutableList<FeatureSpec>,
) {
    if (credits.isEmpty() && features.isEmpty()) {
        return
    }

    item {
        Spacer(
            modifier = Modifier
                .height(8.dp)
                .fillMaxWidth()
                .background(footerBackgroundColor())
        )
    }

    items(features, key = { it.hashCode() }) { feature ->
        FooterItem(
            title = feature.title,
            description = feature.description,
            image = feature.image,
            modifier = Modifier
                .background(footerBackgroundColor())
        )
    }

    items(credits, key = { it.hashCode() }) { credit ->
        FooterItem(
            title = credit.name,
            description = credit.value,
            modifier = Modifier
                .background(footerBackgroundColor())
        )
    }

    item {
        Text(
            text = stringResource(id = L10n.string.ss_copyright, year),
            style = SsTheme.typography.bodySmall.copy(
                fontSize = 15.sp
            ),
            color = if (SsTheme.colors.isDark) {
                SsColors.BaseGrey3
            } else SsColors.BaseGrey2,
            modifier = Modifier
                .fillMaxWidth()
                .background(footerBackgroundColor())
                .padding(
                    horizontal = 16.dp,
                    vertical = 10.dp
                )
        )
    }

    item {
        Spacer(
            modifier = Modifier
                .height(48.dp)
                .fillMaxWidth()
                .background(footerBackgroundColor())
        )
    }

    item {
        Spacer(
            Modifier
                .windowInsetsBottomHeight(WindowInsets.safeDrawing)
                .fillMaxWidth()
                .background(footerBackgroundColor())
        )
    }
}

@Stable
@Composable
internal fun footerBackgroundColor(): Color = if (SsTheme.colors.isDark) {
    Color.Black.lighter()
} else SsColors.BaseGrey1

private val year: String = "© ${Calendar.getInstance().get(Calendar.YEAR)}"

@Composable
private fun FooterItem(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    image: String? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 10.dp
            )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            image?.let {
                FeatureImage(
                    image = image,
                    contentDescription = title,
                    modifier = Modifier
                        .size(
                            width = ImageWidth,
                            height = ImageHeight
                        )
                        .padding(end = 10.dp),
                    tint = SsTheme.colors.primaryForeground,
                    placeholder = {
                        Spacer(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 3.dp)
                                .background(
                                    color = SsTheme.colors.primaryForeground,
                                    shape = CircleShape
                                )
                        )
                    }
                )
            }

            Text(
                text = title,
                style = SsTheme.typography.titleSmall.copy(
                    fontSize = 15.sp
                ),
                color = SsTheme.colors.primaryForeground
            )
        }

        Text(
            text = description,
            style = SsTheme.typography.bodySmall.copy(
                fontSize = 15.sp
            ),
            color = SsTheme.colors.secondaryForeground
        )
    }
}

private val ImageWidth = 26.dp
private val ImageHeight = 22.dp

@PreviewLightDark
@Composable
private fun FeatureItemPreview() {
    SsTheme {
        Surface {
            FooterItem(
                title = "Ellen G. White Quotes",
                description = "Enhance your study with additional selected quotes from Ellen G. White",
                image = "https://images.icon"
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun CreditItemPreview() {
    SsTheme {
        Surface {
            FooterItem(
                title = "Principal Contributor",
                description = "Gavin Anthony"
            )
        }
    }
}
