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

package app.ss.tv.presentation.videos.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.PivotOffsets
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.itemsIndexed
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import app.ss.tv.data.model.CategorySpec
import app.ss.tv.data.model.VideoSpec
import app.ss.tv.presentation.theme.Padding
import app.ss.tv.presentation.theme.rememberChildPadding

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CategoryVideos(
    category: CategorySpec,
    modifier: Modifier = Modifier,
    childPadding: Padding = rememberChildPadding(),
    onVideoClick: (VideoSpec) -> Unit = {},
) {
    var focusedIndex by remember { mutableStateOf<Int?>(null) }
    val isListFocused by remember { derivedStateOf { focusedIndex != null } }
    var subTitle by remember { mutableStateOf<String?>(null) }
    val titleAlpha by animateFloatAsState(if (focusedIndex != null) 1f else 0.70f, label = "title")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
    ) {
            Text(
                text = category.title,
                modifier = Modifier
                    .padding(start = childPadding.start, top = childPadding.top),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = titleAlpha),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Medium,
                ),
            )

            AnimatedVisibility(visible = isListFocused) {
                Text(
                    text = subTitle ?: "",
                    modifier = Modifier
                        .padding(start = childPadding.start, end = childPadding.end, bottom = childPadding.bottom),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.60f),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Medium,
                    ),
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            TvLazyRow(
                modifier = Modifier,
                pivotOffsets = PivotOffsets(parentFraction = 0.07f),
                contentPadding = PaddingValues(
                    start = childPadding.start,
                    top = childPadding.top,
                    end = childPadding.end,
                    bottom = childPadding.bottom
                ),
                verticalAlignment = Alignment.Bottom,
            ) {
                itemsIndexed(category.videos, key = { _, model -> model.id }) { index, video ->
                    var isItemFocused by remember { mutableStateOf(false) }
                    val endPadding by animateDpAsState(if (isItemFocused) 32.dp else 24.dp, label = "padding")

                    VideoRowItem(
                        video = video,
                        onVideoClick = onVideoClick,
                        modifier = Modifier
                            .onFocusChanged {
                                isItemFocused = it.isFocused
                                if (isItemFocused) {
                                    focusedIndex = index
                                }
                            }
                            .focusProperties {
                                if (index == 0) {
                                    left = FocusRequester.Cancel
                                }
                            }
                    )

                    Spacer(modifier = Modifier.width(endPadding))
                }
            }
        }

    LaunchedEffect(key1 = focusedIndex) {
        subTitle = focusedIndex?.let { index -> category.videos[index].title }
    }
}
