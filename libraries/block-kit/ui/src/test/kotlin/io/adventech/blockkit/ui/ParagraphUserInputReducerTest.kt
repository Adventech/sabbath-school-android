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

import androidx.compose.ui.text.TextRange
import io.adventech.blockkit.model.input.Highlight
import io.adventech.blockkit.model.input.HighlightColor
import io.adventech.blockkit.model.input.Underline
import io.adventech.blockkit.model.input.UserInput
import io.adventech.blockkit.model.input.UserInputRequest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ParagraphUserInputReducerTest {

    @Test
    fun `rapid highlight additions include both while repository flow is delayed`() = runTest {
        val repository = DelayedUserInputRepository()
        val reducer = observeRepository(repository)

        repository.save(reducer.addHighlight(HIGHLIGHT_A))
        repository.save(reducer.addHighlight(HIGHLIGHT_B))

        assertEquals(listOf(HIGHLIGHT_A), repository.highlightRequest(0).highlights)
        assertEquals(listOf(HIGHLIGHT_A, HIGHLIGHT_B), repository.highlightRequest(1).highlights)
        assertEquals(emptyList<UserInput>(), repository.input.value)
    }

    @Test
    fun `rapid highlight add then remove retains the new non-overlapping mark`() = runTest {
        val repository = DelayedUserInputRepository(initialHighlights = listOf(HIGHLIGHT_B))
        val reducer = observeRepository(repository)

        repository.save(reducer.addHighlight(HIGHLIGHT_A))
        repository.save(reducer.removeHighlights(HIGHLIGHT_B.range))

        assertEquals(listOf(HIGHLIGHT_B, HIGHLIGHT_A), repository.highlightRequest(0).highlights)
        assertEquals(listOf(HIGHLIGHT_A), repository.highlightRequest(1).highlights)
    }

    @Test
    fun `rapid underline additions include both while repository flow is delayed`() = runTest {
        val repository = DelayedUserInputRepository()
        val reducer = observeRepository(repository)

        repository.save(reducer.toggleUnderline(UNDERLINE_A, UNDERLINE_A.range))
        repository.save(reducer.toggleUnderline(UNDERLINE_B, UNDERLINE_B.range))

        assertEquals(listOf(UNDERLINE_A), repository.underlineRequest(0).underlines)
        assertEquals(listOf(UNDERLINE_A, UNDERLINE_B), repository.underlineRequest(1).underlines)
    }

    @Test
    fun `rapid underline add then remove retains the new non-overlapping mark`() = runTest {
        val repository = DelayedUserInputRepository(initialUnderlines = listOf(UNDERLINE_B))
        val reducer = observeRepository(repository)

        repository.save(reducer.toggleUnderline(UNDERLINE_A, UNDERLINE_A.range))
        repository.save(reducer.removeUnderlines(UNDERLINE_B.range))

        assertEquals(listOf(UNDERLINE_B, UNDERLINE_A), repository.underlineRequest(0).underlines)
        assertEquals(listOf(UNDERLINE_A), repository.underlineRequest(1).underlines)
    }

    @Test
    fun `stale intermediate flow does not roll back the latest local highlight state`() = runTest {
        val repository = DelayedUserInputRepository()
        val reducer = observeRepository(repository)

        repository.save(reducer.addHighlight(HIGHLIGHT_A))
        repository.save(reducer.addHighlight(HIGHLIGHT_B))

        repository.emitSaved(0)
        runCurrent()
        assertEquals(listOf(HIGHLIGHT_A, HIGHLIGHT_B), reducer.highlights)

        repository.emitSaved(1)
        runCurrent()
        assertEquals(listOf(HIGHLIGHT_A, HIGHLIGHT_B), reducer.highlights)
    }

    @Test
    fun `acknowledged flow snapshot initializes a recreated reducer without format changes`() = runTest {
        val repository = DelayedUserInputRepository()
        val reducer = observeRepository(repository)

        repository.save(reducer.addHighlight(HIGHLIGHT_A))
        repository.save(reducer.addHighlight(HIGHLIGHT_B))
        repository.emitSaved(1)
        runCurrent()

        val recreated = observeRepository(repository)
        assertEquals(listOf(HIGHLIGHT_A, HIGHLIGHT_B), recreated.highlights)
        assertEquals(INPUT_ID, repository.highlightInput().id)
    }

    private fun TestScope.observeRepository(
        repository: DelayedUserInputRepository,
    ): ParagraphUserInputReducer {
        val reducer = ParagraphUserInputReducer(
            blockId = BLOCK_ID,
            initialHighlights = emptyList(),
            initialUnderlines = emptyList(),
        )
        backgroundScope.launch {
            repository.input.collectLatest { input ->
                reducer.acceptPersistedHighlights(
                    input.filterIsInstance<UserInput.Highlights>()
                        .firstOrNull { it.blockId == BLOCK_ID }
                        ?.highlights
                        .orEmpty(),
                )
                reducer.acceptPersistedUnderlines(
                    input.filterIsInstance<UserInput.Underlines>()
                        .firstOrNull { it.blockId == BLOCK_ID }
                        ?.underlines
                        .orEmpty(),
                )
            }
        }
        runCurrent()
        return reducer
    }

    private class DelayedUserInputRepository(
        initialHighlights: List<Highlight> = emptyList(),
        initialUnderlines: List<Underline> = emptyList(),
    ) {
        val input = MutableStateFlow(
            buildList {
                if (initialHighlights.isNotEmpty()) {
                    add(UserInput.Highlights(BLOCK_ID, INPUT_ID, TIMESTAMP, initialHighlights))
                }
                if (initialUnderlines.isNotEmpty()) {
                    add(UserInput.Underlines(BLOCK_ID, INPUT_ID, TIMESTAMP, initialUnderlines))
                }
            }
        )
        private val requests = mutableListOf<UserInputRequest>()

        fun save(request: UserInputRequest) {
            requests += request
        }

        fun emitSaved(index: Int) {
            val request = requests[index]
            input.value = input.value.filterNot { persisted ->
                persisted.blockId == request.blockId &&
                    (persisted is UserInput.Highlights && request is UserInputRequest.Highlights ||
                        persisted is UserInput.Underlines && request is UserInputRequest.Underlines)
            } + when (request) {
                is UserInputRequest.Highlights -> UserInput.Highlights(
                    blockId = request.blockId,
                    id = INPUT_ID,
                    timestamp = TIMESTAMP,
                    highlights = request.highlights,
                )
                is UserInputRequest.Underlines -> UserInput.Underlines(
                    blockId = request.blockId,
                    id = INPUT_ID,
                    timestamp = TIMESTAMP,
                    underlines = request.underlines,
                )
                else -> error("Unsupported request: ${request::class.simpleName}")
            }
        }

        fun highlightRequest(index: Int): UserInputRequest.Highlights {
            return requests.filterIsInstance<UserInputRequest.Highlights>()[index]
        }

        fun underlineRequest(index: Int): UserInputRequest.Underlines {
            return requests.filterIsInstance<UserInputRequest.Underlines>()[index]
        }

        fun highlightInput(): UserInput.Highlights {
            return input.value.filterIsInstance<UserInput.Highlights>().single()
        }
    }

    private companion object {
        const val BLOCK_ID = "paragraph-block"
        const val INPUT_ID = "stable-input-id"
        const val TIMESTAMP = 1_735_689_600_000L
        val HIGHLIGHT_A = Highlight(0, 4, 4, HighlightColor.YELLOW)
        val HIGHLIGHT_B = Highlight(10, 14, 4, HighlightColor.BLUE)
        val UNDERLINE_A = Underline(0, 4, 4, HighlightColor.YELLOW)
        val UNDERLINE_B = Underline(10, 14, 4, HighlightColor.BLUE)

        val Highlight.range: TextRange
            get() = TextRange(startIndex, endIndex)

        val Underline.range: TextRange
            get() = TextRange(startIndex, endIndex)
    }
}
