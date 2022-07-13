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

package com.cryart.sabbathschool.bible

import androidx.compose.runtime.Immutable
import com.cryart.sabbathschool.core.model.SSReadingDisplayOptions

@Immutable
internal data class ToolbarSpec(
    val bibleVersions: Set<String>,
    val preferredBible: String,
    val callbacks: ToolbarComponent.Callbacks,
)

@Immutable
internal sealed class ToolbarState {
    data class Success(
        val displayOptions: SSReadingDisplayOptions? = null,
        val bibleVersions: Set<String> = emptySet(),
        val preferredBibleVersion: String
    ) : ToolbarState()

    object Loading : ToolbarState()
    object Error : ToolbarState()
}

@Immutable
internal data class BibleVersesScreenState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val toolbarState: ToolbarState = ToolbarState.Loading,
    val displayOptions: SSReadingDisplayOptions? = null,
    val content: String = ""
)
