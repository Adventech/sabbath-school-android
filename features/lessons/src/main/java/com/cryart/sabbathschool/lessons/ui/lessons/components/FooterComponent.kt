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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cryart.design.theme.BaseGrey2
import app.ss.design.compose.theme.LabelSmall
import com.cryart.design.theme.SSTheme
import app.ss.design.compose.theme.Spacing16
import app.ss.design.compose.theme.Spacing8
import app.ss.design.compose.theme.TitleSmall
import com.cryart.design.theme.onSurfaceSecondaryColor
import com.cryart.design.theme.surfaceSecondaryColor
import com.cryart.sabbathschool.lessons.R
import com.cryart.sabbathschool.lessons.databinding.SsComposeComponentBinding
import com.cryart.sabbathschool.lessons.ui.lessons.components.features.FeatureImage
import com.cryart.sabbathschool.lessons.ui.lessons.components.spec.CreditSpec
import com.cryart.sabbathschool.lessons.ui.lessons.components.spec.FeatureSpec
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

@Immutable
data class LessonsFooterSpec(
    val credits: List<CreditSpec> = emptyList(),
    val features: List<FeatureSpec> = emptyList()
)

class FooterComponent(
    binding: SsComposeComponentBinding,
    private val dataFlow: Flow<LessonsFooterSpec>
) {

    init {
        binding.composeView.setContent {
            SSTheme {
                Surface(color = surfaceSecondaryColor()) {
                    val spec by dataFlow.collectAsState(initial = LessonsFooterSpec())

                    FooterList(spec = spec)
                }
            }
        }
    }
}

@Composable
fun FooterList(
    modifier: Modifier = Modifier,
    spec: LessonsFooterSpec
) {
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

        spec.features.forEach { feature ->
            FeatureFooterItem(feature = feature)
        }

        spec.credits.forEach { credit ->
            CreditFooterItem(credit = credit)
        }

        Text(
            text = stringResource(id = R.string.ss_copyright, year),
            style = LabelSmall.copy(
                color = onSurfaceSecondaryColor()
            ),
            modifier = Modifier.padding(vertical = VerticalPadding)
        )
    }
}

private val year: String = "© ${Calendar.getInstance().get(Calendar.YEAR)}"

@Composable
fun FeatureFooterItem(
    modifier: Modifier = Modifier,
    feature: FeatureSpec
) {
    val imagePaddingEnd = 10.dp

    Column(
        modifier = modifier
            .padding(vertical = VerticalPadding)
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
                style = TitleSmall.copy(color = MaterialTheme.colors.onSurface)
            )
        }

        Text(
            text = feature.description,
            style = LabelSmall.copy(color = BaseGrey2)
        )
    }
}

private val ImageWidth = 26.dp
private val ImageHeight = 22.dp

@Composable
fun CreditFooterItem(
    modifier: Modifier = Modifier,
    credit: CreditSpec
) {
    Column(
        modifier = modifier
            .padding(vertical = VerticalPadding)
    ) {
        Text(
            text = credit.name,
            style = TitleSmall.copy(color = MaterialTheme.colors.onSurface)
        )

        Text(
            text = credit.value,
            style = LabelSmall.copy(color = BaseGrey2)
        )
    }
}

private val VerticalPadding = 10.dp
