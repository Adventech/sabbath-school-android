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

package com.cryart.sabbathschool.lessons.ui.lessons.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.ss.models.Credit
import app.ss.models.Feature
import com.cryart.design.theme.BaseGrey1
import com.cryart.design.theme.BaseGrey2
import com.cryart.design.theme.BaseGrey3
import com.cryart.design.theme.LabelSmall
import com.cryart.design.theme.SSTheme
import com.cryart.design.theme.Spacing16
import com.cryart.design.theme.Spacing8
import com.cryart.design.theme.TitleSmall
import com.cryart.design.theme.lighter
import com.cryart.sabbathschool.lessons.R
import com.cryart.sabbathschool.lessons.databinding.SsComposeComponentBinding
import com.cryart.sabbathschool.lessons.ui.lessons.components.features.FeatureImage
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

data class LessonsFooter(
    val credits: List<Credit> = emptyList(),
    val features: List<Feature> = emptyList()
)

class FooterComponent(
    binding: SsComposeComponentBinding,
    private val dataFlow: Flow<LessonsFooter>
) {

    init {
        binding.composeView.setContent {
            SSTheme {
                Surface(color = footerBackground()) {
                    FooterList(dataFlow = dataFlow)
                }
            }
        }
    }
}

@Composable
fun FooterList(
    modifier: Modifier = Modifier,
    dataFlow: Flow<LessonsFooter>
) {
    val data = dataFlow.collectAsState(initial = LessonsFooter())

    val topPadding = Spacing8
    val horizontalPadding = Spacing16
    val bottomPadding = 48.dp

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                start = horizontalPadding,
                top = topPadding,
                end = horizontalPadding,
                bottom = bottomPadding
            )
    ) {

        data.value.features.forEach { feature ->
            FeatureFooterItem(feature = feature)
        }

        data.value.credits.forEach { credit ->
            CreditFooterItem(credit = credit)
        }

        Text(
            text = stringResource(id = R.string.ss_copyright, year),
            style = LabelSmall.copy(
                color = copyrightColor()
            ),
            modifier = Modifier.padding(
                vertical = VerticalPadding
            )
        )
    }
}

private val year: String = "© ${Calendar.getInstance().get(Calendar.YEAR)}"

@Composable
private fun footerBackground(): Color {
    return if (isSystemInDarkTheme()) {
        Color.Black.lighter()
    } else {
        BaseGrey1
    }
}

@Composable
private fun copyrightColor(): Color {
    return if (isSystemInDarkTheme()) {
        BaseGrey3
    } else {
        BaseGrey2
    }
}

@Composable
fun FeatureFooterItem(
    modifier: Modifier = Modifier,
    feature: Feature
) {
    val imagePaddingEnd = 10.dp

    Column(
        modifier = modifier
            .padding(
                vertical = VerticalPadding,
            )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FeatureImage(
                feature = feature,
                modifier = Modifier
                    .size(
                        width = ImageWidth,
                        height = ImageHeight
                    )
                    .padding(end = imagePaddingEnd),
                tint = MaterialTheme.colors.onSurface
            )

            Text(
                text = feature.title,
                style = TitleSmall.copy(
                    color = MaterialTheme.colors.onSurface
                )
            )
        }

        Text(
            text = feature.description,
            style = LabelSmall.copy(
                color = BaseGrey2
            )
        )
    }
}

private val ImageWidth = 26.dp
private val ImageHeight = 22.dp

@Composable
fun CreditFooterItem(
    modifier: Modifier = Modifier,
    credit: Credit
) {
    Column(
        modifier = modifier
            .padding(
                vertical = VerticalPadding,
            )
    ) {
        Text(
            text = credit.name,
            style = TitleSmall.copy(
                color = MaterialTheme.colors.onSurface
            )
        )

        Text(
            text = credit.value,
            style = LabelSmall.copy(
                color = BaseGrey2
            )
        )
    }
}

private val VerticalPadding = 10.dp
