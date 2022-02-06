/*
 * Copyright (c) 2022. Adventech <info@adventech.io>
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

package com.cryart.sabbathschool.test.di.repository

import app.ss.lessons.data.model.api.SSAudio
import app.ss.lessons.data.model.api.SSVideosInfo
import app.ss.lessons.data.repository.media.MediaRepository
import app.ss.models.media.AudioFile
import com.cryart.sabbathschool.core.response.Resource
import javax.inject.Inject

class FakeMediaRepository @Inject constructor() : MediaRepository {

    override suspend fun getAudio(
        lessonIndex: String
    ): Resource<List<SSAudio>> = Resource.success(emptyList())

    override suspend fun findAudioFile(
        id: String
    ): AudioFile? = null

    override suspend fun updateDuration(
        id: String,
        duration: Long
    ) {}

    override suspend fun getPlayList(
        lessonIndex: String
    ): List<AudioFile> = emptyList()

    override suspend fun getVideo(
        lessonIndex: String
    ): Resource<List<SSVideosInfo>> = Resource.success(emptyList())
}
