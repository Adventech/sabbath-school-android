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

package com.cryart.sabbathschool.lessons.ui.lessons.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import app.ss.models.PublishingInfo
import com.cryart.design.theme.Dimens
import com.cryart.design.theme.LabelXSmall
import com.cryart.design.theme.SSTheme
import com.cryart.design.theme.Spacing16
import com.cryart.design.theme.parse
import com.cryart.sabbathschool.core.extensions.context.launchWebUrl
import com.cryart.sabbathschool.lessons.databinding.SsComposeComponentBinding
import kotlinx.coroutines.flow.Flow

class PublishingInfoComponent(
    binding: SsComposeComponentBinding,
    private val dataFlow: Flow<PublishingInfo?>,
    private val colorFlow: Flow<String?>
) {

    init {
        binding.composeView.setContent {
            val publishingInfo by dataFlow.collectAsState(initial = null)
            val color by colorFlow.collectAsState(initial = null)
            val data = publishingInfo ?: return@setContent
            val primaryColor = color ?: return@setContent

            SSTheme {
                PublishingInfo(
                    publishingInfo = data,
                    primaryColorHex = primaryColor
                )
            }
        }
    }
}

@Composable
private fun PublishingInfo(
    publishingInfo: PublishingInfo,
    primaryColorHex: String,
    context: Context = LocalContext.current
) {
    val onclick: () -> Unit = { context.launchWebUrl(publishingInfo.url) }

    Row(
        modifier = Modifier
            .clickable(
                enabled = true,
                onClick = onclick,
                role = Role.Button
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.size(Dimens.grid_4))

        Text(
            text = publishingInfo.message,
            style = LabelXSmall.copy(
                color = MaterialTheme.colors.onSurface,
            ),
            modifier = Modifier
                .weight(1f)
                .padding(vertical = Spacing16)
        )

        Spacer(modifier = Modifier.size(Spacing16))

        IconButton(
            onClick = onclick,
            modifier = Modifier
                .background(Color.parse(primaryColorHex), CircleShape)
                .size(32.dp)
        ) {
            Icon(
                Icons.Rounded.KeyboardArrowRight,
                contentDescription = "Open",
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.size(Dimens.grid_4))
    }
}
