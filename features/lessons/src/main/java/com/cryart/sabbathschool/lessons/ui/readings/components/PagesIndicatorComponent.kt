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

package com.cryart.sabbathschool.lessons.ui.readings.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.ss.design.compose.extensions.color.parse
import app.ss.design.compose.extensions.previews.DayNightPreviews
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.widget.pager.PagerIndicator
import app.ss.design.compose.widget.pager.PagerState
import com.cryart.sabbathschool.core.extensions.context.isDarkTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import ss.prefs.api.SSPrefs
import ss.prefs.model.SSReadingDisplayOptions

internal class PagesIndicatorComponent(
    composeView: ComposeView,
    ssPrefs: SSPrefs,
    private val onClick: (Int) -> Unit
) {
    private val pageStateFlow = MutableStateFlow(PagerState(0, 0))

    init {
        composeView.setContent {
            SsTheme {
                val state by pageStateFlow.collectAsStateWithLifecycle()
                val displayOptions by ssPrefs.displayOptionsFlow().collectAsStateWithLifecycle(
                    SSReadingDisplayOptions(composeView.context.isDarkTheme())
                )

                Content(
                    currentPage = state.currentPage,
                    pageCount = state.pageCount,
                    theme = displayOptions.theme,
                    onClick = onClick
                )
            }
        }
    }

    fun update(total: Int, selected: Int) {
        pageStateFlow.update { PagerState(selected, total) }
    }

}

@Composable
private fun Content(
    currentPage: Int,
    pageCount: Int,
    theme: String,
    modifier: Modifier = Modifier,
    onClick: (Int) -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                stateDescription = "Page $currentPage of $pageCount"
            },
        contentAlignment = Alignment.Center
    ) {
        PagerIndicator(
            state = PagerState(currentPage, pageCount),
            modifier = Modifier
                .background(
                    backgroundColor(theme = theme).value,
                    RoundedCornerShape(8.dp)
                )
                .padding(vertical = 4.dp, horizontal = 8.dp),
            indicatorSize = 12.dp,
            activeColor = contentColor(theme),
            inActiveColor = Color.LightGray.copy(alpha = 0.35f),
            onClick = onClick
        )
    }
}

@Composable
@Stable
private fun contentColor(theme: String): Color {
    return when (theme) {
        SSReadingDisplayOptions.SS_THEME_DARK -> Color.White
        SSReadingDisplayOptions.SS_THEME_LIGHT -> Color.Black
        SSReadingDisplayOptions.SS_THEME_SEPIA -> Color.parse("#5b4636")
        else -> if (SsTheme.colors.isDark) Color.White else Color.Black
    }
}

@Composable
private fun backgroundColor(theme: String): State<Color> {
    val isDark = SsTheme.colors.isDark
    val colorHex = when (theme) {
        SSReadingDisplayOptions.SS_THEME_LIGHT -> SSReadingDisplayOptions.SS_THEME_LIGHT_HEX
        SSReadingDisplayOptions.SS_THEME_DARK -> SSReadingDisplayOptions.SS_THEME_DARK_HEX
        SSReadingDisplayOptions.SS_THEME_SEPIA -> SSReadingDisplayOptions.SS_THEME_SEPIA_HEX
        else -> if (isDark) SSReadingDisplayOptions.SS_THEME_DARK_HEX else SSReadingDisplayOptions.SS_THEME_LIGHT_HEX
    }
    return animateColorAsState(
        Color.parse(colorHex),
        animationSpec = tween(500), label = ""
    )
}

@DayNightPreviews
@Composable
private fun Preview() {
    SsTheme {
        val themes = listOf(
            SSReadingDisplayOptions.SS_THEME_DARK,
            SSReadingDisplayOptions.SS_THEME_LIGHT,
            SSReadingDisplayOptions.SS_THEME_SEPIA,
            SSReadingDisplayOptions.SS_THEME_DEFAULT,
        )

        Surface {
            LazyColumn(contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp)) {
                items(themes) { theme ->
                    Content(
                        currentPage = 3,
                        pageCount = 10,
                        theme = theme,
                        modifier = Modifier
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
