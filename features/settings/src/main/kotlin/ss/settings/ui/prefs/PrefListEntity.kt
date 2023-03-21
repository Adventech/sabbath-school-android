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

package ss.settings.ui.prefs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import app.ss.design.compose.extensions.content.ContentSpec
import app.ss.design.compose.extensions.list.ListEntity
import app.ss.design.compose.widget.icon.IconSlot

internal sealed interface PrefListEntity : ListEntity {

    @Immutable
    data class Generic(
        override val id: String,
        override val enabled: Boolean = true,
        val title: ContentSpec,
        val icon: IconSlot? = null,
        val summary: ContentSpec? = null,
        val onClick: () -> Unit,
        val withWarning: Boolean = false
    ) : PrefListEntity

    @Immutable
    data class Section(
        val title: ContentSpec,
        override val id: String,
        override val enabled: Boolean = true
    ) : PrefListEntity

    @Immutable
    data class Switch(
        override val id: String,
        override val enabled: Boolean = true,
        val title: ContentSpec,
        val checked: Boolean,
        val onCheckChanged: (Boolean) -> Unit,
        val icon: IconSlot? = null,
        val summary: ContentSpec? = null,
    ) : PrefListEntity

    @Composable
    override fun Content() = PreferenceItem(item = this)

}

