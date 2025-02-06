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
import app.ss.models.AudioAux
import app.ss.models.PDFAux
import app.ss.models.VideoAux
import io.adventech.blockkit.model.feed.FeedGroup
import io.adventech.blockkit.model.feed.FeedType
import io.adventech.blockkit.model.input.UserInput
import io.adventech.blockkit.model.input.UserInputRequest
import io.adventech.blockkit.model.resource.Resource
import io.adventech.blockkit.model.resource.ResourceDocument
import io.adventech.blockkit.model.resource.Segment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import ss.resources.api.ResourcesRepository
import ss.resources.model.FeedModel
import ss.resources.model.LanguageModel
import java.io.File

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

    override suspend fun document(index: String): Result<ResourceDocument> {
        TODO("Not yet implemented")
    }

    override fun documentInput(documentId: String): Flow<List<UserInput>> {
        TODO("Not yet implemented")
    }

    override fun saveDocumentInput(documentId: String, input: UserInputRequest) {
        TODO("Not yet implemented")
    }

    override fun segment(id: String, index: String): Flow<Segment> {
        TODO("Not yet implemented")
    }

    override suspend fun audio(
        resourceIndex: String,
        documentIndex: String
    ): Result<List<AudioAux>> {
        TODO("Not yet implemented")
    }

    override suspend fun video(
        resourceIndex: String,
        documentIndex: String
    ): Result<List<VideoAux>> {
        TODO("Not yet implemented")
    }

    override suspend fun pdf(
        resourceIndex: String,
        documentIndex: String
    ): Result<List<PDFAux>> {
        TODO("Not yet implemented")
    }

    override suspend fun fontFile(name: String): Flow<File?> {
        TODO("Not yet implemented")
    }
}
