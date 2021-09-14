package app.ss.media.playback.ui.common

import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Dismissible(
    onDismiss: () -> Unit,
    directions: Set<DismissDirection> = setOf(DismissDirection.StartToEnd, DismissDirection.EndToStart),
    content: @Composable () -> Unit
) {
    val dismissState = rememberDismissState {
        if (it != DismissValue.Default) {
            onDismiss.invoke()
        }
        true
    }
    SwipeToDismiss(
        state = dismissState,
        directions = directions,
        background = {},
        dismissContent = { content() }
    )
}
