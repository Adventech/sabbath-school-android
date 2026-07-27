/*
 * Copyright (c) 2026. Adventech <info@adventech.io>
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

package io.adventech.blockkit.ui

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import io.adventech.blockkit.model.input.Highlight
import io.adventech.blockkit.model.input.Underline
import io.adventech.blockkit.model.input.UserInputRequest
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/**
 * Reduces paragraph mark mutations against the latest optimistic UI state, independently for each
 * composed block. A lagging persisted emission cannot replace a newer local snapshot; the latest
 * matching emission acknowledges it.
 *
 * This reducer does not provide durable retry or cross-device conflict resolution. Those remain
 * repository responsibilities.
 */
@Stable
internal class ParagraphUserInputReducer(
    private val blockId: String,
    initialHighlights: List<Highlight>,
    initialUnderlines: List<Underline>,
) {
    private var pendingHighlights: ImmutableList<Highlight>? = null
    private var pendingUnderlines: ImmutableList<Underline>? = null

    var highlights: ImmutableList<Highlight> by mutableStateOf(initialHighlights.toImmutableList())
        private set

    var underlines: ImmutableList<Underline> by mutableStateOf(initialUnderlines.toImmutableList())
        private set

    fun acceptPersistedHighlights(highlights: List<Highlight>) {
        val persistedHighlights = highlights.toImmutableList()
        if (pendingHighlights == null || pendingHighlights == persistedHighlights) {
            this.highlights = persistedHighlights
            pendingHighlights = null
        }
    }

    fun acceptPersistedUnderlines(underlines: List<Underline>) {
        val persistedUnderlines = underlines.toImmutableList()
        if (pendingUnderlines == null || pendingUnderlines == persistedUnderlines) {
            this.underlines = persistedUnderlines
            pendingUnderlines = null
        }
    }

    fun addHighlight(highlight: Highlight): UserInputRequest.Highlights {
        return updateHighlights(highlights + highlight)
    }

    fun removeHighlights(selection: TextRange): UserInputRequest.Highlights {
        return updateHighlights(highlights.withoutHighlightsIn(selection))
    }

    fun toggleUnderline(
        underline: Underline,
        selection: TextRange,
    ): UserInputRequest.Underlines {
        val updated = if (underlines.underlinesIn(selection).isNotEmpty()) {
            underlines.withoutUnderlinesIn(selection)
        } else {
            underlines + underline
        }
        return updateUnderlines(updated)
    }

    fun removeUnderlines(selection: TextRange): UserInputRequest.Underlines {
        return updateUnderlines(underlines.withoutUnderlinesIn(selection))
    }

    private fun updateHighlights(updated: List<Highlight>): UserInputRequest.Highlights {
        val local = updated.toImmutableList()
        highlights = local
        pendingHighlights = local
        return UserInputRequest.Highlights(blockId = blockId, highlights = local)
    }

    private fun updateUnderlines(updated: List<Underline>): UserInputRequest.Underlines {
        val local = updated.toImmutableList()
        underlines = local
        pendingUnderlines = local
        return UserInputRequest.Underlines(blockId = blockId, underlines = local)
    }
}

private fun List<Highlight>.withoutHighlightsIn(range: TextRange): List<Highlight> {
    return filterNot { highlight -> highlight.overlaps(range) }
}

private fun List<Underline>.underlinesIn(range: TextRange): List<Underline> {
    return filter { underline -> underline.overlaps(range) }
}

private fun List<Underline>.withoutUnderlinesIn(range: TextRange): List<Underline> {
    return filterNot { underline -> underline.overlaps(range) }
}

private fun Highlight.overlaps(range: TextRange): Boolean {
    return startIndex in range.min..range.max && endIndex in range.min..range.max ||
        startIndex <= range.max && endIndex >= range.min
}

private fun Underline.overlaps(range: TextRange): Boolean {
    return startIndex in range.min..range.max && endIndex in range.min..range.max ||
        startIndex <= range.max && endIndex >= range.min
}
