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

package ss.services.media.ui.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.media3.common.Player
import androidx.media3.common.Player.COMMAND_SEEK_BACK
import androidx.media3.common.Player.COMMAND_SEEK_FORWARD
import androidx.media3.common.listen

@Composable
fun rememberForwardReplayState(player: Player): ForwardReplayState {
    val forwardReplayState = remember(player) { ForwardReplayStateImpl(player) }
    LaunchedEffect(player) { forwardReplayState.observe() }
    return forwardReplayState
}

@Stable
interface ForwardReplayState {
    val isForwardEnabled: Boolean
    val isReplayEnabled: Boolean

    fun forward()
    fun replay()
}

@Immutable
internal class ForwardReplayStateImpl(private val player: Player): ForwardReplayState {

    override var isForwardEnabled by mutableStateOf(player.isCommandAvailable(COMMAND_SEEK_FORWARD))
        private set

    override var isReplayEnabled by mutableStateOf(player.isCommandAvailable(COMMAND_SEEK_BACK))
        private set

    override fun forward() {
        player.seekForward()
    }

    override fun replay() {
        player.seekBack()
    }

    suspend fun observe(): Unit = player.listen { events ->
        if (events.containsAny(Player.EVENT_AVAILABLE_COMMANDS_CHANGED)) {
            isForwardEnabled = isCommandAvailable(COMMAND_SEEK_FORWARD)
            isReplayEnabled = isCommandAvailable(COMMAND_SEEK_BACK)
        }
    }

    override fun toString(): String {
        return "ForwardReplayState(isForwardEnabled=$isForwardEnabled, isReplayEnabled=$isReplayEnabled)"
    }
}
