/*
 * Copyright (c) 2022. Adventech <info@adventech.io>
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.ss.design.compose.theme.Dimens
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.theme.onSurfaceSecondary
import app.ss.design.compose.theme.parse
import app.ss.design.compose.widget.divider.Divider
import app.ss.design.compose.widget.icon.IconButton
import com.cryart.sabbathschool.core.extensions.context.launchWebUrl
import com.cryart.sabbathschool.lessons.ui.lessons.components.spec.PublishingInfoSpec
import app.ss.translations.R.string as RString

internal fun LazyListScope.publishingInfo(
    publishingInfo: PublishingInfoSpec?,
) {
    publishingInfo?.let {
        item {
            Surface {
                PublishingInfo(
                    spec = publishingInfo,
                    modifier = Modifier
                        .animateItemPlacement()
                )
            }
        }
    }
}

@Composable
private fun PublishingInfo(
    spec: PublishingInfoSpec,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val onclick: () -> Unit = { context.launchWebUrl(spec.url) }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .clickable(
                    enabled = true,
                    onClick = onclick,
                    role = Role.Button
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = spec.message,
                style = MaterialTheme.typography.bodySmall,
                color = onSurfaceSecondary(),
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 16.dp)
                    .padding(start = Dimens.grid_4, end = 16.dp)
            )

            IconButton(
                onClick = onclick,
                modifier = Modifier
                    .background(Color.parse(spec.primaryColorHex), CircleShape)
                    .size(32.dp),
                stateLayerSize = 48.dp,
            ) {
                Icon(
                    Icons.Rounded.KeyboardArrowRight,
                    contentDescription = stringResource(id = RString.ss_action_open),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.size(Dimens.grid_4))
        }

        Divider()
    }
}

@Preview(name = "Publishing Info")
@Composable
private fun PreviewPublishingInfo() {
    SsTheme {
        Surface {
            PublishingInfo(
                spec = PublishingInfoSpec(
                    message = "The right to print and distribute this Sabbath School resource in the " +
                        "United States belongs to the Pacific Press Publishing Association.",
                    url = "http://www.sabbathschoolmaterials.com",
                    primaryColorHex = "#385bb2"
                ),
            )
        }
    }
}
