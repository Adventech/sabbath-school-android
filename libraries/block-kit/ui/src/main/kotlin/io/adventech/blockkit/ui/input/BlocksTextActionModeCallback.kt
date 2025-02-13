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
import android.view.Menu
import android.view.MenuItem
import androidx.compose.ui.geometry.Rect
import io.adventech.blockkit.model.input.HighlightColor
import app.ss.translations.R as L10nR
import io.adventech.blockkit.ui.R as BlockKitR

internal class BlocksTextActionModeCallback(
    val onActionModeDestroy: (() -> Unit)? = null,
    var rect: Rect = Rect.Zero,
    var onCopyRequested: (() -> Unit)? = null,
    var onPasteRequested: (() -> Unit)? = null,
    var onCutRequested: (() -> Unit)? = null,
    var onSelectAllRequested: (() -> Unit)? = null,
    var onHighlightRequested: (() -> Unit)? = null,
    var onHighlightColorRequested: ((HighlightColor) -> Unit)? = null,
    var onRemoveHighlightRequested: (() -> Unit)? = null,
) {

    var viewMode: Mode = Mode.NONE

    fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        requireNotNull(menu) { "onCreateActionMode requires a non-null menu" }
        requireNotNull(mode) { "onCreateActionMode requires a non-null mode" }

        return when (viewMode) {
            Mode.HIGHLIGHTS -> {
                showHighlights(mode, menu)
                true
            }

            Mode.TEXT -> {
                onHighlightRequested?.let {
                    addMenuItem(menu, MenuItemOption.Highlight)
                }
                onCopyRequested?.let {
                    addMenuItem(menu, MenuItemOption.Copy)
                }
                onPasteRequested?.let {
                    addMenuItem(menu, MenuItemOption.Paste)
                }
                onCutRequested?.let {
                    addMenuItem(menu, MenuItemOption.Cut)
                }
                onSelectAllRequested?.let {
                    addMenuItem(menu, MenuItemOption.SelectAll)
                }
                true
            }

            Mode.NONE -> {
                menu.clear()
                mode.hide(100L)
                false
            }
        }
    }

    // this method is called to populate new menu items when the actionMode was invalidated
    fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        if (mode == null || menu == null || viewMode == Mode.NONE) return false
        updateMenuItems(mode, menu)
        // should return true so that new menu items are populated
        return true
    }

    fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        when (item?.itemId) {
            MenuItemOption.Copy.id -> onCopyRequested?.invoke()
            MenuItemOption.Paste.id -> onPasteRequested?.invoke()
            MenuItemOption.Cut.id -> onCutRequested?.invoke()
            MenuItemOption.SelectAll.id -> onSelectAllRequested?.invoke()
            MenuItemOption.Highlight.id -> onHighlightRequested?.invoke()
            BlockKitR.id.highlight_blue -> {
                onHighlightColorRequested?.invoke(HighlightColor.BLUE)
            }

            BlockKitR.id.highlight_green -> {
                onHighlightColorRequested?.invoke(HighlightColor.GREEN)
            }

            BlockKitR.id.highlight_orange -> {
                onHighlightColorRequested?.invoke(HighlightColor.ORANGE)
            }

            BlockKitR.id.highlight_yellow -> {
                onHighlightColorRequested?.invoke(HighlightColor.YELLOW)
            }
            BlockKitR.id.highlight_remove -> {
                onRemoveHighlightRequested?.invoke()
            }
            else -> return false
        }
        mode?.finish()
        return true
    }

    fun onDestroyActionMode() {
        onActionModeDestroy?.invoke()
    }

    private fun updateMenuItems(mode: ActionMode, menu: Menu) {
        when (viewMode) {
            Mode.HIGHLIGHTS -> showHighlights(mode, menu)
            Mode.TEXT -> {
                addOrRemoveMenuItem(menu, MenuItemOption.Highlight, onHighlightRequested)
                addOrRemoveMenuItem(menu, MenuItemOption.Copy, onCopyRequested)
                addOrRemoveMenuItem(menu, MenuItemOption.Paste, onPasteRequested)
                addOrRemoveMenuItem(menu, MenuItemOption.Cut, onCutRequested)
                addOrRemoveMenuItem(menu, MenuItemOption.SelectAll, onSelectAllRequested)
            }

            Mode.NONE -> menu.clear()
        }
    }

    internal fun addMenuItem(menu: Menu, item: MenuItemOption) {
        menu.add(0, item.id, item.order, item.titleResource)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
    }

    private fun addOrRemoveMenuItem(
        menu: Menu,
        item: MenuItemOption,
        callback: (() -> Unit)?
    ) {
        when {
            callback != null && menu.findItem(item.id) == null -> addMenuItem(menu, item)
            callback == null && menu.findItem(item.id) != null -> menu.removeItem(item.id)
        }
    }

    private fun showHighlights(mode: ActionMode, menu: Menu?) {
        requireNotNull(menu) { "showHighlights requires a non-null menu" }
        menu.clear()
        val inflater = mode.menuInflater
        inflater.inflate(BlockKitR.menu.selection_highlights, menu)
    }

    enum class Mode {
        HIGHLIGHTS,
        TEXT,
        NONE
    }
}

internal enum class MenuItemOption(val id: Int) {
    Copy(0),
    Paste(1),
    Cut(2),
    SelectAll(3),
    Highlight(4),
    ;

    val titleResource: Int
        get() = when (this) {
            Copy -> android.R.string.copy
            Paste -> android.R.string.paste
            Cut -> android.R.string.cut
            SelectAll -> android.R.string.selectAll
            Highlight -> L10nR.string.ss_action_highlight
        }

    /**
     * This item will be shown before all items that have order greater than this value.
     */
    val order = id
}
