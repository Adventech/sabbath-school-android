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

package ss.resources.api

import app.ss.models.AudioAux
import app.ss.models.PDFAux
import app.ss.models.VideoAux
import io.adventech.blockkit.model.feed.FeedGroup
import io.adventech.blockkit.model.feed.FeedType
import io.adventech.blockkit.model.resource.Resource
import io.adventech.blockkit.model.resource.ResourceDocument
import kotlinx.coroutines.flow.Flow
import ss.resources.model.FeedModel
import ss.resources.model.LanguageModel
import java.io.File

interface ResourcesRepository {
    fun languages(query: String? = null): Flow<List<LanguageModel>>

    fun language(code: String): Flow<LanguageModel>

    suspend fun feed(type: FeedType): Result<FeedModel>

    suspend fun feedGroup(id: String, type: FeedType): Result<FeedGroup>

    suspend fun resource(index: String): Result<Resource>

    suspend fun document(index: String): Result<ResourceDocument>

    suspend fun audio(resourceIndex: String, documentIndex: String): Result<List<AudioAux>>

    suspend fun video(resourceIndex: String, documentIndex: String): Result<List<VideoAux>>

    suspend fun pdf(resourceIndex: String, documentIndex: String): Result<List<PDFAux>>

    suspend fun fontFile(name: String): Flow<File?>
}
