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

import android.view.ActionMode
import android.view.View
import androidx.annotation.DoNotInline
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus

class BlocksTextToolbar(
    private val view: View,
    private val onHighlight: () -> Unit = {},
) : TextToolbar {

    private var actionMode: ActionMode? = null
    private val textActionModeCallback: BlocksTextActionModeCallback = BlocksTextActionModeCallback(
        onActionModeDestroy = {
            actionMode = null
        },
    )
    override var status: TextToolbarStatus = TextToolbarStatus.Hidden

    override fun showMenu(
        rect: Rect,
        onCopyRequested: (() -> Unit)?,
        onPasteRequested: (() -> Unit)?,
        onCutRequested: (() -> Unit)?,
        onSelectAllRequested: (() -> Unit)?
    ) {
        textActionModeCallback.rect = rect
        textActionModeCallback.onCopyRequested = onCopyRequested
        textActionModeCallback.onCutRequested = onCutRequested
        textActionModeCallback.onPasteRequested = onPasteRequested
        textActionModeCallback.onSelectAllRequested = onSelectAllRequested
        textActionModeCallback.onCommentRequested = {
            // Dismiss the action mode here and remove text selection
            // Not implemented yet
        }
        textActionModeCallback.onHighlightRequested = {
            // Dismiss the action mode here and remove text selection
            onHighlight()
        }
        if (actionMode == null) {
            status = TextToolbarStatus.Shown
            actionMode = BlocksTextToolbarHelperMethods.startActionMode(
                view,
                BlocksFloatingTextActionModeCallback(textActionModeCallback),
                ActionMode.TYPE_FLOATING
            )
        } else {
            actionMode?.invalidate()
        }
    }

    override fun hide() {
        status = TextToolbarStatus.Hidden
        actionMode?.finish()
        actionMode = null
    }
}

/**
 * This class is here to ensure that the classes that use this API will get verified and can be
 * AOT compiled. It is expected that this class will soft-fail verification, but the classes
 * which use this method will pass.
 */
internal object BlocksTextToolbarHelperMethods {
    @DoNotInline
    fun startActionMode(
        view: View,
        actionModeCallback: ActionMode.Callback,
        type: Int
    ): ActionMode? {
        return view.startActionMode(
            actionModeCallback,
            type
        )
    }

    @DoNotInline
    fun invalidateContentRect(actionMode: ActionMode) {
        actionMode.invalidateContentRect()
    }
}
