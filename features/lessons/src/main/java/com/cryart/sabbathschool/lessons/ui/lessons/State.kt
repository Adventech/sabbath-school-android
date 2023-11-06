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

package com.cryart.sabbathschool.lessons.ui.lessons

import androidx.compose.runtime.Immutable
import app.ss.models.PublishingInfo
import app.ss.models.SSQuarterlyInfo

sealed class PublishingInfoState {
    @Immutable
    data class Success(val publishingInfo: PublishingInfo) : PublishingInfoState()
    data object Loading : PublishingInfoState()
    data object Error : PublishingInfoState()
}

sealed class QuarterlyInfoState {
    @Immutable
    data class Success(
        val quarterlyInfo: SSQuarterlyInfo,
        val onOfflineStateClick: () -> Unit,
    ) : QuarterlyInfoState()

    data object Loading : QuarterlyInfoState()
    data object Error : QuarterlyInfoState()
}

@Immutable
data class LessonsScreenState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val quarterlyInfo: QuarterlyInfoState = QuarterlyInfoState.Loading,
    val publishingInfo: PublishingInfoState = PublishingInfoState.Loading
)

internal val LessonsScreenState.quarterlyTitle: String get() =
    (quarterlyInfo as? QuarterlyInfoState.Success)?.quarterlyInfo?.quarterly?.title ?: ""
