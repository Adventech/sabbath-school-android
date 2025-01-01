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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.adventech.blockkit.model.BlockItem
import io.adventech.blockkit.model.ReferenceScope
import io.adventech.blockkit.ui.style.LatoFontFamily
import io.adventech.blockkit.ui.style.Styler
import io.adventech.blockkit.ui.style.theme.BlocksPreviewTheme
import ss.ui.placeholder.asPlaceholder

@Composable
internal fun ReferenceContent(blockItem: BlockItem.Reference, modifier: Modifier = Modifier) {
    val textColor = Styler.textColor(null)

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable {},
        shape = Styler.roundedShape(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Styler.genericBackgroundColorForInteractiveBlock()
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            blockItem.resource?.covers?.portrait?.takeUnless { blockItem.isHiddenSegment() }?.let {
                val placeholder: @Composable () -> Unit = {
                    Spacer(
                        modifier = Modifier
                            .fillMaxSize()
                            .asPlaceholder(
                                visible = true,
                                color = MaterialTheme.colorScheme.inverseOnSurface,
                                highlightColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)
                            )
                    )
                }
                AsyncImageBox(
                    data = it,
                    contentDescription = blockItem.title,
                    modifier = Modifier
                        .size(60.dp, 90.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    loading = placeholder,
                    error = placeholder,
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = blockItem.title,
                    style = TextStyle(
                        fontFamily = LatoFontFamily,
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                blockItem.subtitle?.let {
                    Text(
                        text = it,
                        style = TextStyle(
                            fontFamily = LatoFontFamily,
                            color = textColor.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = textColor
            )
        }
    }
}

private fun BlockItem.Reference.isHiddenSegment(): Boolean {
    return scope == ReferenceScope.SEGMENT && segment != null
}

@PreviewLightDark
@Composable
private fun Preview() {
    BlocksPreviewTheme {
        Surface {
            ReferenceContent(
                blockItem = BlockItem.Reference(
                    id = "1",
                    style = null,
                    data = null,
                    nested = false,
                    segment = null,
                    target = "",
                    scope = ReferenceScope.DOCUMENT,
                    title = "Teacher Comments",
                    subtitle = "Quarterly Lesson",
                    resource = null,
                    document = null,
                )
            )
        }
    }
}
