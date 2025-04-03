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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.media3.common.Player
import androidx.media3.common.listen
import androidx.media3.common.text.Cue
import androidx.media3.common.util.UnstableApi
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@Composable
fun rememberPlaybackCuesState(player: Player): PlaybackCuesState {
    val playbackCuesState = remember(player) { PlaybackCuesStateImpl(player) }
    LaunchedEffect(player) { playbackCuesState.observe() }
    return playbackCuesState
}

@Stable
interface PlaybackCuesState {
    val cues: ImmutableList<Cue>
}

internal class PlaybackCuesStateImpl(private val player: Player): PlaybackCuesState {

    override var cues by mutableStateOf<ImmutableList<Cue>>(persistentListOf())
        private set

    @androidx.annotation.OptIn(UnstableApi::class)
    suspend fun observe(): Unit = player.listen { events ->
        if (events.contains(Player.EVENT_CUES)) {
            cues = currentCues.cues.toImmutableList()
        }
    }

    override fun toString(): String {
        return "PlaybackCuesState(cues=$cues)"
    }
}
