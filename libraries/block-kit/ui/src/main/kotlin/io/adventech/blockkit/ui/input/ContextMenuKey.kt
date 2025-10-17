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

package io.adventech.blockkit.ui.input

/**
 * Represents keys for context menu actions.
 */
internal sealed interface ContextMenuKey {
    /**
     * Key for highlight action.
     */
    data object HighlightKey : ContextMenuKey

    /**
     * Key for underline action.
     */
    data object UnderlineKey : ContextMenuKey

    /**
     * Key for search action.
     */
    data object SearchKey : ContextMenuKey

    /**
     * Highlight color options for context menu.
     */
    sealed interface Highlight : ContextMenuKey {
        data object Blue : Highlight
        data object Yellow : Highlight
        data object Orange : Highlight
        data object Green : Highlight
        data object Purple : Highlight
        data object Brown : Highlight
        data object Red : Highlight
        data object Remove : Highlight
    }

    /**
     * Underline color options for context menu.
     */
    sealed interface Underline : ContextMenuKey {
        data object Blue : Underline
        data object Yellow : Underline
        data object Orange : Underline
        data object Green : Underline
        data object Purple : Underline
        data object Brown : Underline
        data object Red : Underline
        data object Remove : Underline
    }
}


/**
 * Enum representing the type of context menu.
 *
 * - DEFAULT: Standard context menu.
 * - HIGHLIGHT: Menu for highlight actions.
 * - UNDERLINE: Menu for underline actions.
 */
internal enum class ContextMenuType {
    DEFAULT,
    HIGHLIGHT,
    UNDERLINE,
}
