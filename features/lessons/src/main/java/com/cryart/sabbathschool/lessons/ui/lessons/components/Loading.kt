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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.ss.design.compose.extensions.modifier.asPlaceholder
import app.ss.design.compose.extensions.previews.DevicePreviews
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.widget.divider.Divider

internal fun LazyListScope.loading() {
    item {
        LessonsLoading(modifier = Modifier)
    }
}

@Composable
private fun LessonsLoading(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 80.dp, bottom = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(
            modifier = Modifier
                .size(width = 145.dp, height = 210.dp)
                .asPlaceholder(true, shape = ProgressShape)
        )

        Spacer(modifier = Modifier.height(height = 24.dp))

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(height = 20.dp)
                .padding(horizontal = 48.dp)
                .asPlaceholder(true, shape = ProgressShape)
        )

        Spacer(modifier = Modifier.height(height = 24.dp))

        Spacer(
            modifier = Modifier
                .size(120.dp, height = 12.dp)
                .asPlaceholder(true, shape = ProgressShape)
        )

        Spacer(modifier = Modifier.height(height = 24.dp))

        Spacer(
            modifier = Modifier
                .size(340.dp, height = 8.dp)
                .asPlaceholder(true, shape = ProgressShape)
        )

        Spacer(modifier = Modifier.height(height = 4.dp))

        Spacer(
            modifier = Modifier
                .size(340.dp, height = 8.dp)
                .asPlaceholder(true, shape = ProgressShape)
        )

        Spacer(modifier = Modifier.height(height = 4.dp))

        Spacer(
            modifier = Modifier
                .size(180.dp, height = 8.dp)
                .asPlaceholder(true, shape = ProgressShape)
        )

        Spacer(modifier = Modifier.height(height = 32.dp))

        Divider()

        for (i in 0 until 4) {
            LoadingListItem()
        }
    }
}

@Composable
private fun LoadingListItem() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.height(height = 16.dp))

        Spacer(
            modifier = Modifier
                .size(240.dp, height = 14.dp)
                .padding(horizontal = 16.dp)
                .asPlaceholder(true, shape = ProgressShape)
        )

        Spacer(modifier = Modifier.height(height = 4.dp))

        Spacer(
            modifier = Modifier
                .size(100.dp, height = 8.dp)
                .padding(horizontal = 16.dp)
                .asPlaceholder(true, shape = ProgressShape)
        )

        Spacer(modifier = Modifier.height(height = 12.dp))

        Divider()
    }
}

private val ProgressShape = RoundedCornerShape(16.dp)

@DevicePreviews
@Composable
private fun LoadingPreview() {
    SsTheme {
        Surface {
            LessonsLoading()
        }
    }
}
