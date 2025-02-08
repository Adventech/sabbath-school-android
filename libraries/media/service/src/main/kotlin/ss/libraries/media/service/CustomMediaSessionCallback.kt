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

package ss.libraries.media.service

import android.content.Context
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import app.ss.translations.R as L10nR
import ss.libraries.media.resources.R as MediaR

private const val CUSTOM_COMMAND_REWIND = "REWIND"
private const val CUSTOM_COMMAND_FORWARD = "FORWARD"

internal class CustomMediaSessionCallback(
    private val context: Context,
    private val id: String,
) : MediaSession.Callback {

    val customCommands: List<CommandButton> by lazy {
        listOf(
            getRewindCommandButton(SessionCommand("$id-$CUSTOM_COMMAND_REWIND", Bundle.EMPTY)),
            getForwardCommandButton(SessionCommand("$id-$CUSTOM_COMMAND_FORWARD", Bundle.EMPTY)),
        )
    }

    @OptIn(UnstableApi::class)
    override fun onConnect(session: MediaSession, controller: MediaSession.ControllerInfo): MediaSession.ConnectionResult {
        val availableSessionCommands =
            MediaSession.ConnectionResult.DEFAULT_SESSION_AND_LIBRARY_COMMANDS.buildUpon()
        for (commandButton in customCommands) {
            // Add custom command to available session commands.
            commandButton.sessionCommand?.let { availableSessionCommands.add(it) }
        }
        return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
            .setAvailableSessionCommands(availableSessionCommands.build())
            .build()
    }

    override fun onCustomCommand(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
        customCommand: SessionCommand,
        args: Bundle
    ): ListenableFuture<SessionResult> {
        when (customCommand.customAction) {
            "$id-$CUSTOM_COMMAND_REWIND" -> session.player.seekBack()
            "$id-$CUSTOM_COMMAND_FORWARD" -> session.player.seekForward()
        }
        return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
    }

    private fun getRewindCommandButton(sessionCommand: SessionCommand): CommandButton {
        return CommandButton.Builder()
            .setDisplayName(context.getString(L10nR.string.ss_action_rewind))
            .setSessionCommand(sessionCommand)
            .setIconResId(MediaR.drawable.ic_audio_icon_backward)
            .build()
    }

    private fun getForwardCommandButton(sessionCommand: SessionCommand): CommandButton {
        return CommandButton.Builder()
            .setDisplayName(context.getString(L10nR.string.ss_action_forward))
            .setSessionCommand(sessionCommand)
            .setIconResId(MediaR.drawable.ic_audio_icon_forward)
            .build()
    }
}
