/*
 * Copyright (c) 2024. Adventech <info@adventech.io>
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

package ss.lessons.api.repository

import app.ss.models.LessonIntroModel
import app.ss.models.PublishingInfo
import app.ss.models.QuarterlyGroup
import app.ss.models.SSQuarterly
import app.ss.models.SSQuarterlyInfo
import kotlinx.coroutines.flow.Flow

interface QuarterliesRepository {

    fun getQuarterly(index: String): Flow<Result<SSQuarterlyInfo>>

    fun getQuarterlies(
        languageCode: String? = null,
        group: QuarterlyGroup? = null,
    ): Flow<Result<List<SSQuarterly>>>

    fun getPublishingInfo(): Flow<Result<PublishingInfo?>>

    suspend fun getIntro(index: String): Result<LessonIntroModel?>
}
