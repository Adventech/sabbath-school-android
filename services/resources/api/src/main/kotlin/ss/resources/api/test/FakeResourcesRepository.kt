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

package ss.resources.api.test

import androidx.annotation.VisibleForTesting
import app.ss.models.feed.FeedGroup
import app.ss.models.feed.FeedType
import app.ss.models.resource.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import ss.resources.api.ResourcesRepository
import ss.resources.model.FeedModel
import ss.resources.model.LanguageModel

@VisibleForTesting(otherwise = VisibleForTesting.NONE)
class FakeResourcesRepository(
    private val languagesFlow: Flow<List<LanguageModel>> = emptyFlow()
) : ResourcesRepository {

    override fun languages(query: String?): Flow<List<LanguageModel>> = languagesFlow

    override fun language(code: String): Flow<LanguageModel> {
        TODO("Not yet implemented")
    }

    override suspend fun feed(type: FeedType): Result<FeedModel> {
        TODO("Not yet implemented")
    }

    override suspend fun feedGroup(id: String, type: FeedType): Result<FeedGroup> {
        TODO("Not yet implemented")
    }

    override suspend fun resource(index: String): Result<Resource> {
        TODO("Not yet implemented")
    }
}
