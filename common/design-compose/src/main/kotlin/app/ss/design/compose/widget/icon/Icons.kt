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

package app.ss.design.compose.widget.icon

import androidx.annotation.StringRes
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.material.icons.Icons as MaterialIcons
import app.ss.translations.R.string as Translations

enum class Icons(
    private val imageVector: ImageVector,
    @StringRes private val contentDescription: Int?,
) : IconSlot {

    AccountCircle(MaterialIcons.Rounded.AccountCircle, Translations.ss_account),
    ArrowBack(MaterialIcons.Rounded.ArrowBack, Translations.ss_action_arrow_back),
    ArrowDropDown(MaterialIcons.Rounded.ArrowDropDown, Translations.ss_action_arrow_drop_down),
    ArrowRight(MaterialIcons.Rounded.KeyboardArrowRight, Translations.ss_action_arrow_right),
    Cancel(MaterialIcons.Rounded.Cancel, android.R.string.cancel),
    Check(MaterialIcons.Rounded.Check, Translations.ss_action_selected),
    Close(MaterialIcons.Rounded.Close, Translations.ss_action_close);

    @Composable
    override fun Content(contentColor: Color, modifier: Modifier) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription?.let { stringResource(id = it) },
            tint = contentColor,
            modifier = modifier
        )
    }
}
