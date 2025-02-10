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

package ss.lessons.test

import androidx.annotation.VisibleForTesting
import app.ss.models.media.SSAudio
import retrofit2.Response
import ss.lessons.api.SSMediaApi
import ss.lessons.model.VideosInfoModel

/** Fake implementation of [SSMediaApi]. **/
@VisibleForTesting(otherwise = VisibleForTesting.NONE)
class FakeMediaApi : SSMediaApi {

    // region Mock behavior

    private var audioMap: MutableMap<String, Response<List<SSAudio>>> = mutableMapOf()
    private var videoMap: MutableMap<String, Response<List<VideosInfoModel>>> = mutableMapOf()
    private var languages: MutableSet<String> = mutableSetOf()

    fun addAudioResponse(language: String, response: Response<List<SSAudio>>) {
        audioMap[language] = response
    }

    fun addVideoResponse(language: String, response: Response<List<VideosInfoModel>>) {
        videoMap[language] = response
    }

    fun addLanguage(language: String) {
        languages.add(language)
    }

    // end region

    override suspend fun getAudio(
        language: String,
        quarterlyId: String
    ): Response<List<SSAudio>> = audioMap[language]!!

    override suspend fun getVideoLanguages(): Response<List<String>> = Response.success(languages.toList())

    override suspend fun getLatestVideo(
        language: String
    ): Response<List<VideosInfoModel>> = videoMap[language]!!
}
