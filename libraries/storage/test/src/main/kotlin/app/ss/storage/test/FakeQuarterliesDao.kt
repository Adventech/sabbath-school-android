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

import androidx.annotation.VisibleForTesting
import app.ss.models.OfflineState
import app.ss.models.QuarterlyGroup
import app.ss.storage.db.dao.QuarterliesDao
import app.ss.storage.db.entity.QuarterlyEntity
import app.ss.storage.db.entity.QuarterlyInfoEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** Fake implementation of [QuarterliesDao] for use in tests. */
@VisibleForTesting
class FakeQuarterliesDao : QuarterliesDao {

    private val languageEntityMap: MutableMap<String, List<QuarterlyEntity>> = mutableMapOf()
    var infoEntitiesForSync: List<QuarterlyInfoEntity> = emptyList()

    var lastUpdatedQuarterly: QuarterlyEntity? = null
        private set

    fun addQuarterlies(language: String, quarterlies: List<QuarterlyEntity>) {
        languageEntityMap[language] = quarterlies
    }

    override fun get(language: String): List<QuarterlyEntity> {
        return languageEntityMap[language] ?: emptyList()
    }

    override fun get(language: String, group: QuarterlyGroup): List<QuarterlyEntity> {
        return languageEntityMap[language] ?: emptyList()
    }

    override fun getFlow(language: String): Flow<List<QuarterlyEntity>> {
        return flowOf(languageEntityMap[language] ?: emptyList())
    }

    override fun getFlow(language: String, group: QuarterlyGroup): Flow<List<QuarterlyEntity>> {
        return flowOf(languageEntityMap[language] ?: emptyList())
    }

    override fun getAllForSync(): List<QuarterlyInfoEntity> = infoEntitiesForSync

    override fun getInfo(quarterlyIndex: String): QuarterlyInfoEntity? {
        TODO("Not yet implemented")
    }

    override fun getInfoFlow(quarterlyIndex: String): Flow<QuarterlyInfoEntity?> {
        TODO("Not yet implemented")
    }

    override suspend fun getCovers(language: String): List<String> {
        TODO("Not yet implemented")
    }

    override suspend fun getOfflineState(index: String): OfflineState? {
        TODO("Not yet implemented")
    }

    override suspend fun setOfflineState(index: String, state: OfflineState) {
        TODO("Not yet implemented")
    }

    override suspend fun insertItem(item: QuarterlyEntity) = Unit

    override suspend fun insertAll(items: List<QuarterlyEntity>) = Unit

    override suspend fun update(item: QuarterlyEntity) {
        lastUpdatedQuarterly = item
    }
}
