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

package app.ss.media.playback

import app.ss.media.playback.model.AudioFile
import app.ss.media.repository.SSMediaRepository

interface AudioQueueManager {
    var currentAudioIndex: Int
    val currentAudioId: String
    var currentAudio: AudioFile?

    val previousAudioIndex: Int?
    val nextAudioIndex: Int?
    val queue: List<AudioFile>

    suspend fun refreshCurrentAudio(): AudioFile?

    fun setCurrentAudioId(audioId: String)
    fun setAudioQueue(queue: List<AudioFile>, selected: Int)
}

internal class AudioQueueManagerImpl(
    private val repository: SSMediaRepository
) : AudioQueueManager {

    private var audioId: String? = null
    private val queueList = mutableListOf<AudioFile>()

    override var currentAudioIndex: Int = 0

    override val currentAudioId: String get() = audioId ?: ""

    override var currentAudio: AudioFile? = null

    override val previousAudioIndex: Int? = null

    override val nextAudioIndex: Int? = null
    override val queue: List<AudioFile> get() = queueList

    override suspend fun refreshCurrentAudio(): AudioFile? {
        val id = audioId ?: return null
        currentAudio = repository.findAudioFile(id)

        return currentAudio
    }

    override fun setCurrentAudioId(audioId: String) {
        this.audioId = audioId
    }

    override fun setAudioQueue(queue: List<AudioFile>, selected: Int) {
        queueList.clear()
        queueList.addAll(queue)

        currentAudio = queueList.getOrNull(selected)
        audioId = currentAudio?.id
    }
}
