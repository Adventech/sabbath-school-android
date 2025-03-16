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

package ss.lessons.test

import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import ss.lessons.api.ContentSyncProvider

/** Fake implementation of [ContentSyncProvider]. **/
@VisibleForTesting(otherwise = VisibleForTesting.NONE)
class FakeContentSyncProvider : ContentSyncProvider {

    var defaultSyncResult: Result<Unit>? = null

    private var syncResultMap: MutableMap<String, Result<Unit>> = mutableMapOf()

    fun addSyncResult(index: String, result: Result<Unit>) {
        syncResultMap[index] = result
    }

    override suspend fun syncQuarterlies(): Result<Unit> = defaultSyncResult!!

    override suspend fun removeAllDownloads(): Result<Unit> = defaultSyncResult!!

    override fun hasDownloads(): Flow<Boolean> = flowOf(true)
}
