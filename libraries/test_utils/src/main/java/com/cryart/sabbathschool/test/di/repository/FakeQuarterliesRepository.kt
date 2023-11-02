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

package com.cryart.sabbathschool.test.di.repository

import app.ss.lessons.data.repository.quarterly.QuarterliesRepository
import app.ss.models.Language
import app.ss.models.LessonIntroModel
import app.ss.models.PublishingInfo
import app.ss.models.SSQuarterlyInfo
import com.cryart.sabbathschool.core.response.Resource
import com.cryart.sabbathschool.test.di.mock.QuarterlyMockData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class FakeQuarterliesRepository @Inject constructor(
    private val mockData: QuarterlyMockData
) : QuarterliesRepository {

    override fun getLanguages(query: String?): Flow<Resource<List<Language>>> {
        return flowOf(Resource.success(emptyList()))
    }

    override fun getQuarterlyInfo(index: String): Flow<Resource<SSQuarterlyInfo>> {
        return flowOf(
            mockData.getQuarterlyInfo(index)?.let {
                Resource.success(it)
            } ?: Resource.error(Throwable())
        )
    }

    override fun getPublishingInfo(languageCode: String?): Flow<Resource<PublishingInfo>> {
        return emptyFlow()
    }

    override suspend fun getIntro(index: String): Result<LessonIntroModel?> {
        return Result.success(null)
    }
}
