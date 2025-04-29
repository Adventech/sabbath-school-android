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

package ss.document.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import app.ss.design.compose.extensions.modifier.asPlaceholder
import app.ss.design.compose.extensions.window.containerWidth
import app.ss.design.compose.theme.SsTheme

private val horizontalPadding = 16.dp
private val cornerRadius = 10.dp

@Composable
fun DocumentLoadingView(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
) {
    val screenWidth = LocalWindowInfo.current.containerWidth()

    LazyColumn(
        modifier = modifier,
        state = state,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        item {
            Spacer(
                Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .asPlaceholder(true, shape = RectangleShape))
        }

        item {
            LoadingRectangle(
                Modifier
                    .size(
                        width = (screenWidth * 0.8f).dp,
                        height = 20.dp
                    )
                    .padding(horizontal = horizontalPadding)
            )
        }

        item {
            LoadingRectangle(
                Modifier
                    .size(
                        width = (screenWidth * 0.57f).dp,
                        height = 15.dp
                    )
                    .padding(horizontal = horizontalPadding)
            )
        }

        itemsIndexed(listOf(1, 2, 3)) { index, item ->
            LoadingRectangle(
                Modifier
                    .size(
                        width = (screenWidth * if (item == 2) 0.5f else 0.9f).dp,
                        height = 8.dp
                    )
                    .padding(horizontal = horizontalPadding)
            )
        }

        items(13) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding)
            ) {
                LoadingRectangle(
                    Modifier.size(
                        width = 15.dp,
                        height = 15.dp
                    )
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    LoadingRectangle(
                        Modifier.size(
                            width = (screenWidth * 0.6f).dp,
                            height = 15.dp
                        )
                    )
                    LoadingRectangle(
                        Modifier.size(
                            width = (screenWidth * 0.3f).dp,
                            height = 10.dp
                        )
                    )
                }
            }
        }

    }
}

@Composable
private fun LoadingRectangle(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .asPlaceholder(true, shape = RoundedCornerShape(cornerRadius))
    )
}

@PreviewLightDark
@Composable
private fun Preview() {
    SsTheme {
        Surface { DocumentLoadingView() }
    }
}

