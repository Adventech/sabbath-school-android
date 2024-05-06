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

package app.ss.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ss.design.compose.extensions.content.ContentSpec
import app.ss.design.compose.theme.LatoFontFamily
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.theme.color.SsColors
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.overlay.ContentWithOverlays
import com.slack.circuit.overlay.LocalOverlayHost
import com.slack.circuitx.overlays.DialogResult
import dagger.hilt.components.SingletonComponent
import ss.libraries.circuit.navigation.LoginScreen
import ss.libraries.circuit.overlay.ssAlertDialogOverlay
import app.ss.auth.R as AuthR
import app.ss.translations.R as L10nR

@CircuitInject(LoginScreen::class, SingletonComponent::class)
@Composable
fun LoginScreenUI(state: State, modifier: Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(22.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(modifier = Modifier.weight(0.2f))

        Image(
            painter = painterResource(id = AuthR.drawable.ic_logo_sspm_scaled),
            contentDescription = null,
            colorFilter = ColorFilter.tint(
                color = if (SsTheme.colors.isDark) Color.White else SsColors.BaseBlue
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(id = L10nR.string.ss_app_name),
            style = SsTheme.typography.headlineSmall,
            color = if (SsTheme.colors.isDark) Color.White else SsColors.BaseBlue
        )

        Spacer(modifier = Modifier.weight(1f))

        Buttons(
            enabled = state != State.Loading,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            eventSink = when (state) {
                is State.ConfirmSignInAnonymously,
                State.Loading -> { _ -> }

                is State.Default -> state.eventSink
                is State.Error -> state.eventSink
            }
        )

        Spacer(modifier = Modifier.weight(0.2f))

        // Show the confirm overlay
        (state as? State.ConfirmSignInAnonymously)?.let { OverlayContent(state) }
    }
}

@Composable
private fun Buttons(
    enabled: Boolean,
    modifier: Modifier = Modifier,
    eventSink: (Event) -> Unit = {},
) {
    Column(modifier = modifier.width(270.dp)) {

        Button(
            onClick = { eventSink(Event.SignInWithGoogle) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            enabled = enabled,
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = SsColors.BaseGrey3
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp
            )
        ) {

            Icon(
                painter = painterResource(id = AuthR.drawable.ic_google_logo),
                contentDescription = null,
                tint = Color.Unspecified
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Sign in with Google",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }

        TextButton(
            onClick = { eventSink(Event.SignInAnonymously) },
            modifier = Modifier
                .fillMaxWidth(),
            enabled = enabled,
        ) {
            Text(
                text = stringResource(id = L10nR.string.ss_login_button_anonymous),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = SsTheme.colors.primaryForeground
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        val textColor = SsTheme.colors.primaryForeground
        val annotatedText by remember {
            mutableStateOf(
                buildAnnotatedString {
                    append("By continuing, you agree to our Terms of Service as described in our ")
                    pushStringAnnotation(
                        tag = "URL", annotation = PRIVACY_POLICY_URL
                    )
                    withStyle(
                        style = SpanStyle(
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            fontFamily = LatoFontFamily,
                        )
                    ) {
                        append("Privacy Policy.")
                    }
                    append(" Sabbath School collects User IDs to help identify and restore user saved content.")
                }
            )
        }
        ClickableText(
            text = annotatedText,
            modifier = Modifier.fillMaxWidth(),
            style = SsTheme.typography.bodySmall.copy(fontSize = 11.sp, color = textColor),
        ) { offset ->
            annotatedText.getStringAnnotations(
                tag = "URL", start = offset, end = offset
            ).firstOrNull()?.let { _ ->
                eventSink(Event.OpenPrivacyPolicy)
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OverlayContent(state: State.ConfirmSignInAnonymously) {
    val overlayHost = LocalOverlayHost.current
    LaunchedEffect(Unit) {
        val result = overlayHost.show(
            ssAlertDialogOverlay(
                title = ContentSpec.Res(L10nR.string.ss_login_anonymously_dialog_title),
                cancelText = ContentSpec.Res(L10nR.string.ss_login_anonymously_dialog_negative),
                confirmText = ContentSpec.Res(L10nR.string.ss_login_anonymously_dialog_positive),
                content = {
                    Text(text = stringResource(id = L10nR.string.ss_login_anonymously_dialog_description))
                },
                usePlatformDefaultWidth = true,
            )
        )

        when (result) {
            DialogResult.Confirm -> state.onConfirm()
            DialogResult.Cancel,
            DialogResult.Dismiss -> state.onDecline()
        }
    }
}

@PreviewLightDark
@Composable
private fun UiPreview() {
    SsTheme(useDynamicTheme = false) {
        Surface {
            LoginScreenUI(
                state = State.Default {},
                modifier = Modifier,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun UiPreviewOverlay() {
    SsTheme {
        ContentWithOverlays {
            Surface {
                LoginScreenUI(
                    state = State.ConfirmSignInAnonymously({}, {}),
                    modifier = Modifier,
                )
            }
        }
    }
}
