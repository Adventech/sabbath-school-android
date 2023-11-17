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

package app.ss.design.compose.widget.icon

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.ss.design.compose.R
import app.ss.translations.R.string as L10n

/**
 * An [IconSlot] from a [DrawableRes]
 */
@Immutable
data class ResIcon(
    @DrawableRes val res: Int,
    @StringRes val contentDescription: Int?
) : IconSlot {

    @Composable
    override fun Content(contentColor: Color, modifier: Modifier) {
        Icon(
            painter = painterResource(id = res),
            contentDescription = contentDescription?.let { stringResource(id = it) },
            tint = contentColor,
            modifier = modifier
        )
    }

    companion object {
        val Download = ResIcon(R.drawable.ic_icon_download, L10n.blank)
        val Downloaded = ResIcon(R.drawable.ic_icon_downloaded, L10n.blank)
        val Downloading = ResIcon(R.drawable.ic_icon_downloading, L10n.blank)
        val Github = ResIcon(R.drawable.github, L10n.ss_about_github)
        val Instagram = ResIcon(R.drawable.instagram, L10n.ss_settings_instagram)
    }
}
