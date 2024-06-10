package app.ss.quarterlies.overlay

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.theme.color.SsColors
import app.ss.design.compose.widget.icon.IconBox
import app.ss.design.compose.widget.icon.Icons
import app.ss.quarterlies.R
import com.slack.circuit.overlay.Overlay
import com.slack.circuit.overlay.OverlayNavigator
import app.ss.translations.R.string as L10nR

class AppBrandingOverlay : Overlay<Unit> {
    @Composable
    override fun Content(navigator: OverlayNavigator<Unit>) {
        Dialog(
            content = {
                Surface(
                    shape = AlertDialogDefaults.shape,
                    color = AlertDialogDefaults.containerColor,
                    tonalElevation = AlertDialogDefaults.TonalElevation,
                ) {
                    DialogContent(onDismiss = { navigator.finish(Unit) })
                }
            },
            onDismissRequest = {
                navigator.finish(Unit)
            },
        )
    }
}

@Composable
private fun DialogContent(onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {

            Image(
                painter = painterResource(id = R.drawable.ic_logo_sspm_scaled),
                contentDescription = null,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .size(72.dp)
                    .align(Alignment.Center),
                colorFilter = ColorFilter.tint(
                    if (SsTheme.colors.isDark) Color.White else SsColors.BaseBlue
                )
            )

            IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.TopEnd)) {
                IconBox(icon = Icons.Close)
            }
        }

        Text(
            text = stringResource(id = L10nR.sspm_re_branding_message),
            modifier = Modifier.fillMaxWidth(),
            style = SsTheme.typography.bodySmall.copy(
                fontSize = 16.sp,
                letterSpacing = 0.01.sp,
                lineHeight = 24.sp
            ),
            textAlign = TextAlign.Center
        )

        Image(
            bitmap = ImageBitmap.imageResource(id = R.drawable.gc_sspm_logo),
            contentDescription = null,
            modifier = Modifier.size(160.dp, 80.dp),
            colorFilter = ColorFilter.tint(
                if (SsTheme.colors.isDark) Color.White else SsColors.BaseBlue
            )
        )
    }
}

