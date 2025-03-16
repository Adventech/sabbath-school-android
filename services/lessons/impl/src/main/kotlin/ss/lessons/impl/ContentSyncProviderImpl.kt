/*
 * Copyright (c) 2025. Adventech <info@adventech.io>
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

package ss.lessons.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import ss.foundation.coroutines.DispatcherProvider
import ss.lessons.api.ContentSyncProvider
import ss.libraries.storage.api.dao.LessonsDao
import ss.libraries.storage.api.dao.ReadsDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ContentSyncProviderImpl @Inject constructor(
    private val readsDao: ReadsDao,
    private val lessonsDao: LessonsDao,
    private val dispatcherProvider: DispatcherProvider,
) : ContentSyncProvider {

    override suspend fun syncQuarterlies(): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun removeAllDownloads(): Result<Unit> {
        lessonsDao.deleteAll()
        readsDao.deleteAll()
        return syncQuarterlies()
    }

    override fun hasDownloads(): Flow<Boolean> = combine(lessonsDao.getAll(), readsDao.getAll()) { lessons, reads ->
        lessons.isNotEmpty() && reads.isNotEmpty()
    }.flowOn(dispatcherProvider.io)
}
