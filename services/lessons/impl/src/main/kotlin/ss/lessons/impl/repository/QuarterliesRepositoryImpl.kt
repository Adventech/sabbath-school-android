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

package ss.lessons.impl.repository

import app.ss.models.SSQuarterlyInfo
import kotlinx.coroutines.withContext
import ss.foundation.coroutines.DispatcherProvider
import ss.foundation.coroutines.Scopable
import ss.foundation.coroutines.ioScopable
import ss.lessons.api.helper.SyncHelper
import ss.lessons.api.repository.QuarterliesRepository
import ss.lessons.impl.ext.toModel
import ss.libraries.storage.api.dao.QuarterliesDao
import javax.inject.Inject

internal class QuarterliesRepositoryImpl @Inject constructor(
    private val quarterliesDao: QuarterliesDao,
    private val syncHelper: SyncHelper,
    private val dispatcherProvider: DispatcherProvider,
) : QuarterliesRepository, Scopable by ioScopable(dispatcherProvider) {

    override suspend fun getQuarterlyInfo(index: String): Result<SSQuarterlyInfo> {
        val cached = withContext(dispatcherProvider.io) {
            quarterliesDao.getInfo(index)?.takeUnless { it.lessons.isEmpty() }
        }

        return if (cached == null) {
            syncHelper.syncQuarterlyInfo(index)?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Failed to sync quarterly info"))
        } else {
            Result.success(
                SSQuarterlyInfo(
                    quarterly = cached.quarterly.toModel(),
                    lessons = cached.lessons.sortedBy { it.order }.map { it.toModel() }
                )
            )
        }
    }

}
