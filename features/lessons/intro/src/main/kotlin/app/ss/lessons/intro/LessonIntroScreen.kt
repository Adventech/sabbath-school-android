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

package app.ss.lessons.intro

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.widget.DragHandle
import app.ss.design.compose.widget.placeholder.PlaceholderScreen

@Composable
internal fun LessonIntroRoute(
    viewModel: LessonIntroViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Surface {
        AnimatedContent(targetState = state, label = "") { targetState ->
            when (targetState) {
                is LessonIntroState.Success -> LessonIntroScreen(state = targetState)
                LessonIntroState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        PlaceholderScreen()

                        DragHandle(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 16.dp)
                        )
                    }
                }
                LessonIntroState.Error -> { /* No op */ }
            }
        }
    }
}

@Composable
internal fun LessonIntroScreen(
    state: LessonIntroState.Success
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            DragHandle()
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Text(
                text = state.title,
                style = SsTheme.typography.titleLarge
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Text(
                text = state.introduction,
                style = SsTheme.typography.bodyMedium
            )
        }
    }
}
