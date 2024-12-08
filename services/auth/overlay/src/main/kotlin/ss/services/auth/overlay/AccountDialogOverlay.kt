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

package ss.services.auth.overlay

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import app.ss.design.compose.extensions.modifier.asPlaceholder
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.widget.content.ContentBox
import app.ss.design.compose.widget.divider.Divider
import app.ss.design.compose.widget.icon.IconBox
import app.ss.design.compose.widget.icon.Icons
import app.ss.design.compose.widget.image.RemoteImage
import com.slack.circuit.overlay.Overlay
import com.slack.circuit.overlay.OverlayNavigator
import app.ss.translations.R.string as L10nR

class AccountDialogOverlay(
    private val userInfo: UserInfo,
) : Overlay<AccountDialogOverlay.Result> {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(navigator: OverlayNavigator<Result>) {
        BasicAlertDialog(
            content = {
                Surface(
                    shape = AlertDialogDefaults.shape,
                    color = AlertDialogDefaults.containerColor,
                    tonalElevation = AlertDialogDefaults.TonalElevation,
                ) {
                    DialogContent(userInfo = userInfo, modifier = Modifier, onResult = navigator::finish)
                }
            },
            onDismissRequest = {
                navigator.finish(Result.Dismiss)
            },
        )
    }

    sealed interface Result {
        data object Dismiss : Result
        data object SignOut : Result
        data object GoToSettings : Result
        data class ShareApp(val context: Context) : Result
        data object GoToAbout : Result
        data class GoToPrivacyPolicy(val context: Context) : Result
    }
}

@Composable
private fun DialogContent(userInfo: UserInfo, modifier: Modifier = Modifier, onResult: (AccountDialogOverlay.Result) -> Unit = { }) {
    val context = LocalContext.current
    val listItemColors = ListItemDefaults.colors(
        containerColor = AlertDialogDefaults.containerColor
    )

    Column(modifier = modifier.padding(vertical = 16.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ContentBox(
                content = RemoteImage(
                    data = userInfo.photo,
                    contentDescription = null,
                    loading = {
                        Spacer(
                            modifier = Modifier
                                .size(AccountImgSize)
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
                                .size(AccountImgSize)

                        )
                    }
                ),
                modifier = Modifier
                    .size(AccountImgSize)
                    .clip(CircleShape),
            )

            Column {
                Text(
                    text = userInfo.displayName ?: stringResource(L10nR.ss_menu_anonymous_name),
                    style = SsTheme.typography.bodyMedium
                )
                Text(
                    text = userInfo.email ?: stringResource(L10nR.ss_menu_anonymous_email),
                    style = SsTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        FilledTonalButton(
            onClick = { onResult(AccountDialogOverlay.Result.SignOut) },
            modifier = Modifier.padding(start = 80.dp, top = 8.dp)
        ) {
            Text(text = stringResource(L10nR.ss_menu_sign_out))
        }

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        ListItem(
            headlineContent = {
                Text(
                    text = stringResource(id = L10nR.ss_settings),
                    style = SsTheme.typography.titleMedium
                )
            },
            modifier = Modifier.clickable { onResult(AccountDialogOverlay.Result.GoToSettings) },
            leadingContent = {
                IconBox(
                    icon = Icons.Settings,
                    modifier = Modifier
                )
            },
            colors = listItemColors,
        )

        ListItem(
            headlineContent = {
                Text(
                    text = stringResource(id = L10nR.ss_menu_share_app),
                    style = SsTheme.typography.titleMedium
                )
            },
            modifier = Modifier.clickable { onResult(AccountDialogOverlay.Result.ShareApp(context)) },
            leadingContent = {
                IconBox(
                    icon = Icons.Share,
                    modifier = Modifier
                )
            },
            colors = listItemColors,
        )

        ListItem(
            headlineContent = {
                Text(
                    text = stringResource(id = L10nR.ss_about),
                    style = SsTheme.typography.titleMedium
                )
            },
            modifier = Modifier.clickable { onResult(AccountDialogOverlay.Result.GoToAbout) },
            leadingContent = {
                IconBox(
                    icon = Icons.Info,
                    modifier = Modifier
                )
            },
            colors = listItemColors,
        )

        ListItem(
            headlineContent = {
                Text(
                    text = stringResource(id = L10nR.ss_privacy_policy),
                    style = SsTheme.typography.titleMedium
                )
            },
            modifier = Modifier.clickable { onResult(AccountDialogOverlay.Result.GoToPrivacyPolicy(context)) },
            leadingContent = {
                IconBox(
                    icon = Icons.Privacy,
                    modifier = Modifier
                )
            },
            colors = listItemColors,
        )

    }
}

private val AccountImgSize = 48.dp

@PreviewLightDark
@Composable
private fun PreviewDialogContent() {
    SsTheme {
        Surface(color = AlertDialogDefaults.containerColor) {
            DialogContent(userInfo = UserInfo())
        }
    }
}
