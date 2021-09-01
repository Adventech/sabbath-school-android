/*
 * Copyright (c) 2021. Adventech <info@adventech.io>
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
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package app.ss.media.playback.repository

import androidx.core.net.toUri
import app.ss.media.playback.model.AudioFile
import com.cryart.sabbathschool.core.extensions.coroutines.SchedulerProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

interface AudioRepository {
    suspend fun findAudioFile(id: String): AudioFile?
}

internal class AudioRepositoryImpl(
    private val schedulerProvider: SchedulerProvider
) : AudioRepository, CoroutineScope by MainScope() {

    override suspend fun findAudioFile(id: String): AudioFile? {
        // TODO: Fetch correct Audio
        return AudioFile(
            id = "876d52b6d4193883a43dba72ecd4d5d4c0b775b24fded652f3b667e1dfb0066e",
            title = "Worn and Weary",
            artist = "Adult Bible Study Guides",
            source = (
                "https://sabbath-school-media.adventech.io/audio/en/2021-03/876d52b6d4193883a43dba" +
                    "72ecd4d5d4c0b775b24fded652f3b667e1dfb0066e/876d52b6d4193883a43dba72ecd4d5d4c0b775b" +
                    "24fded652f3b667e1dfb0066e.mp3"
                ).toUri(),
            target = "en/2021-03/01/02",
            targetIndex = "en-2021-03-01-02",
            image = "https://sabbath-school.adventech.io/api/v1/en/quarterlies/2021-03/cover.png",
            imageRatio = "portrait",
        )
    }
}
