package app.ss.media.playback.ui.common

import androidx.compose.material.BottomSheetState
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import app.ss.media.playback.PlaybackConnection
import kotlinx.coroutines.flow.Flow

val LocalPlaybackConnection = staticCompositionLocalOf<PlaybackConnection> {
    error("No LocalPlaybackConnection provided")
}

@OptIn(ExperimentalMaterialApi::class)
val LocalPlaybackSheetState = staticCompositionLocalOf<BottomSheetState> {
    // error("No LocalPlaybackSheetState provided")
    BottomSheetState(initialValue = BottomSheetValue.Collapsed)
}

@Composable
fun <T> rememberFlowWithLifecycle(
    flow: Flow<T>,
    lifecycle: Lifecycle = LocalLifecycleOwner.current.lifecycle,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED
): Flow<T> = remember(flow, lifecycle) {
    flow.flowWithLifecycle(
        lifecycle = lifecycle,
        minActiveState = minActiveState
    )
}
