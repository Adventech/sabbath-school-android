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

import androidx.media3.common.C
import androidx.media3.common.Tracks
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import ss.services.media.ui.spec.SimpleTrack

internal fun Tracks.asSimpleTracks(): ImmutableList<SimpleTrack> {
    val simpleTracks = mutableListOf<SimpleTrack>()

    for (trackGroup in groups) {
        // Group level information.
        val trackType = trackGroup.type
        for (i in 0 until trackGroup.length) {
            // Individual track information.
            val isSupported = trackGroup.isTrackSupported(i)
            if (!isSupported) continue
            val isSelected = trackGroup.isTrackSelected(i)
            val trackFormat = trackGroup.getTrackFormat(i)
            val id = trackFormat.id ?: continue
            val language = trackFormat.language ?: continue
            val label = trackFormat.label?.replaceFirstChar { it.uppercase() } ?: continue

            when (trackType) {
                C.TRACK_TYPE_AUDIO -> SimpleTrack.Audio(id, language, label, isSelected)
                C.TRACK_TYPE_TEXT -> SimpleTrack.Subtitle(id, language, label, isSelected)
                else -> null
            }?.let { simpleTrack ->
                simpleTracks.add(simpleTrack)
            }
        }
    }

    return simpleTracks.toImmutableList()
}

