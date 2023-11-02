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
import app.ss.models.SSQuarterly
import app.ss.models.SSQuarterlyInfo
import retrofit2.Response
import ss.lessons.api.SSQuarterliesApi
import ss.lessons.model.PublishingInfoData
import ss.lessons.model.SSLanguage
import ss.lessons.model.request.PublishingInfoRequest

/** Fake implementation of [SSQuarterliesApi] for use in tests. */
@VisibleForTesting
class FakeQuarterliesApi : SSQuarterliesApi {
    override suspend fun getLanguages(): Response<List<SSLanguage>> {
        TODO("Not yet implemented")
    }

    override suspend fun getQuarterlies(language: String): Response<List<SSQuarterly>> {
        TODO("Not yet implemented")
    }

    override suspend fun getQuarterlyInfo(language: String, id: String): Response<SSQuarterlyInfo> {
        TODO("Not yet implemented")
    }

    override suspend fun getPublishingInfo(request: PublishingInfoRequest): Response<PublishingInfoData> {
        TODO("Not yet implemented")
    }
}
