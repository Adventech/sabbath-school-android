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

package ss.feed.components.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import app.ss.design.compose.extensions.isLargeScreen
import app.ss.design.compose.extensions.modifier.asPlaceholder
import app.ss.design.compose.extensions.window.containerWidth
import app.ss.design.compose.theme.SsTheme
import io.adventech.blockkit.model.feed.FeedDirection
import io.adventech.blockkit.model.feed.FeedView
import io.adventech.blockkit.model.resource.ResourceCoverType
import ss.feed.components.coverSize

private val horizontalPadding = 16.dp
private val cornerRadius = 10.dp
private val spaceBetweenCoverAndTitle = 16.dp

@Composable
internal fun FeedLoadingView(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(horizontal = horizontalPadding, vertical = 20.dp),
) {
    val shimmerEffect = Modifier.asPlaceholder(true)
    val screenWidth = LocalWindowInfo.current.containerWidth()

    LazyColumn(
        modifier = modifier.navigationBarsPadding(),
        state = state,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding),
                horizontalArrangement = Arrangement.Start
            ) {
                Box(
                    modifier = Modifier
                        .width((screenWidth * 0.4f).dp)
                        .height(30.dp)
                        .clip(RoundedCornerShape(cornerRadius))
                        .then(shimmerEffect)
                )
            }
        }

        items(5) { position ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding),
                horizontalArrangement = Arrangement.spacedBy(spaceBetweenCoverAndTitle),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(coverSize(ResourceCoverType.PORTRAIT, FeedDirection.VERTICAL, FeedView.FOLIO, screenWidth, isLargeScreen()))
                        .clip(RoundedCornerShape(cornerRadius))
                        .then(shimmerEffect)
                )

                // Randomized width
                val pair = remember(position) {
                    mutableStateOf((150..200).random() to (150..200).random())
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    val (top, bottom) = pair.value
                    repeat(2) {
                        val size = if (it == 0) top else bottom

                        Box(
                            modifier = Modifier
                                .height(15.dp)
                                .width(size.dp)
                                .clip(RoundedCornerShape(cornerRadius))
                                .then(shimmerEffect)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun Preview() {
    SsTheme { Surface { FeedLoadingView() } }
}
