/*
 * Copyright (c) 2024. Adventech <info@adventech.io>
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

package app.ss.design.compose.widget.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.ss.design.compose.extensions.modifier.asPlaceholder
import app.ss.design.compose.widget.content.ContentBox
import app.ss.design.compose.widget.icon.IconBox
import app.ss.design.compose.widget.icon.Icons
import app.ss.design.compose.widget.image.RemoteImage
import app.ss.translations.R.string as L10nR

@Composable
fun AvatarNavigationIcon(
    photoUrl: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    ContentBox(
        content = RemoteImage(
            data = photoUrl,
            contentDescription = stringResource(id = L10nR.ss_account),
            loading = {
                Spacer(
                    modifier = Modifier
                        .size(AvatarSize)
                        .asPlaceholder(
                            visible = true,
                            shape = CircleShape
                        )
                )
            },
            error = {
                IconBox(
                    icon = Icons.AccountCircle,
                    modifier = Modifier
                        .size(AvatarSize)
                        .clickable { onClick() }
                )
            }
        ),
        modifier = modifier
            .padding(horizontal = 16.dp)
            .size(AvatarSize)
            .clip(CircleShape)
            .clickable { onClick() }
    )
}

private val AvatarSize = 32.dp
