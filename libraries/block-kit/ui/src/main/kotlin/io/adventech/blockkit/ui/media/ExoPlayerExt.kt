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

package io.adventech.blockkit.ui.media

import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer

@OptIn(UnstableApi::class)
internal fun ExoPlayer.availableTracks(): List<SimpleTrack> {
    val hasTracks =
        isCommandAvailable(Player.COMMAND_GET_TRACKS) && !currentTracks.isEmpty

    if (!hasTracks) {
        return emptyList()
    }

    return currentTracks.groups.flatMap { trackGroup ->
        (0 until trackGroup.length).mapNotNull { i ->
            val format = trackGroup.getTrackFormat(i)
            val id = format.id
            when {
                id?.startsWith("audio-high") == true -> {
                    format.language?.let { language ->
                        format.label?.let { label ->
                            SimpleTrack.Audio(id, language, label.replaceFirstChar { it.uppercase() })
                        }
                    }
                }

                id?.startsWith("subs") == true -> {
                    format.language?.let { language ->
                        format.label?.let { label ->
                            SimpleTrack.Subtitle(id, language, label.replaceFirstChar { it.uppercase() })
                        }
                    }
                }

                else -> null
            }
        }
    }
}

internal sealed interface SimpleTrack {
    val id: String
    val language: String
    val label: String

    data class Audio(
        override val id: String,
        override val language: String,
        override val label: String,
    ) : SimpleTrack

    data class Subtitle(
        override val id: String,
        override val language: String,
        override val label: String,
    ) : SimpleTrack
}
