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

package com.cryart.sabbathschool.lessons.ui.lessons.components.features

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import app.ss.design.compose.extensions.modifier.asPlaceholder
import app.ss.design.compose.extensions.previews.DayNightPreviews
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.widget.content.ContentBox
import app.ss.design.compose.widget.image.RemoteImage
import com.cryart.sabbathschool.lessons.ui.lessons.components.spec.FeatureSpec

@Immutable
internal data class QuarterlyFeaturesSpec(
    val features: List<FeatureSpec>
)

@Composable
internal fun QuarterlyFeaturesRow(
    modifier: Modifier = Modifier,
    spec: QuarterlyFeaturesSpec
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(
                start = 16.dp,
                bottom = 16.dp,
                end = 16.dp
            )
    ) {
        spec.features.forEach { feature ->
            FeatureImage(
                image = feature.image,
                contentDescription = feature.title,
                modifier = Modifier.size(
                    width = ImageWidth,
                    height = ImageHeight
                )
            )

            Spacer(modifier = Modifier.size(12.dp))
        }
    }
}

private val ImageWidth = 16.dp
private val ImageHeight = 12.dp

@Composable
internal fun FeatureImage(
    image: String,
    contentDescription: String,
    modifier: Modifier,
    tint: Color = Color.White.copy(alpha = 0.5f),
    placeholder: @Composable () -> Unit = {
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 2.dp)
                .asPlaceholder(visible = true, shape = CircleShape)
        )
    }
) {
    ContentBox(
        content = RemoteImage(
            data = image,
            contentDescription = contentDescription,
            contentScale = ContentScale.Inside,
            colorFilter = ColorFilter.tint(tint),
            loading = placeholder,
            error = placeholder
        ),
        modifier = modifier
    )
}

@DayNightPreviews
@Composable
fun FeaturesRowPreview() {
    SsTheme {
        Surface {
            QuarterlyFeaturesRow(
                spec = QuarterlyFeaturesSpec(
                    features = listOf(
                        FeatureSpec(
                            image = "https://sabbath-school.adventech.io/api/v1/images/features/feature_egw.png",
                            name = "",
                            title = "",
                            description = ""

                        ),
                        FeatureSpec(
                            name = "",
                            title = "",
                            description = "",
                            image = "https://sabbath-school.adventech.io/api/v1/images/features/feature_inside_story.png"
                        )
                    )
                )
            )
        }
    }
}
