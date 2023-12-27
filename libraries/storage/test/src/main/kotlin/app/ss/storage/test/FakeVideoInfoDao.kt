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

package app.ss.storage.test

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import ss.libraries.storage.api.dao.VideoInfoDao
import ss.libraries.storage.api.entity.VideoInfoEntity

/**
 * Fake implementation of [VideoInfoDao] for use in tests.
 */
class FakeVideoInfoDao(
    private val videoInfoFlow: MutableStateFlow<List<VideoInfoEntity>> = MutableStateFlow(emptyList())
) : VideoInfoDao {

    override fun get(lessonIndex: String): List<VideoInfoEntity> {
        return videoInfoFlow.value.filter { it.lessonIndex == lessonIndex }
    }

    override fun getAsFlow(lessonIndex: String): Flow<List<VideoInfoEntity>> = videoInfoFlow

    override suspend fun delete(index: String) {
        videoInfoFlow.update { entities -> entities.dropWhile { it.lessonIndex == index } }
    }

    override suspend fun insertItem(item: VideoInfoEntity) {
        videoInfoFlow.update { it.toMutableList() + item }
    }

    override suspend fun insertAll(items: List<VideoInfoEntity>) {
        videoInfoFlow.update { it.toMutableList() + items }
    }

    override suspend fun update(item: VideoInfoEntity) {
        videoInfoFlow.update { entities ->
            entities.map { if (it.lessonIndex == item.lessonIndex) item else it }
        }
    }
}
