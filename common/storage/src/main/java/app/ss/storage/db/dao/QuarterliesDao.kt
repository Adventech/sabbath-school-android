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

package app.ss.storage.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import app.ss.models.OfflineState
import app.ss.models.QuarterlyGroup
import app.ss.storage.db.entity.QuarterlyEntity
import app.ss.storage.db.entity.QuarterlyInfoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuarterliesDao : BaseDao<QuarterlyEntity> {

    @Query("SELECT * FROM quarterlies WHERE lang = :language")
    fun get(language: String): List<QuarterlyEntity>

    @Query("SELECT * FROM quarterlies WHERE lang = :language")
    fun getFlow(language: String): Flow<List<QuarterlyEntity>>

    @Transaction
    @Query("SELECT * FROM quarterlies WHERE offlineState = 'NONE' OR offlineState = 'PARTIAL'")
    fun getAllForSync(): List<QuarterlyInfoEntity>

    @Query("SELECT * FROM quarterlies WHERE lang = :language AND quarterly_group = :group")
    fun get(language: String, group: QuarterlyGroup): List<QuarterlyEntity>

    @Query("SELECT * FROM quarterlies WHERE lang = :language AND quarterly_group = :group")
    fun getFlow(language: String, group: QuarterlyGroup): Flow<List<QuarterlyEntity>>

    @Transaction
    @Query("SELECT * FROM quarterlies WHERE quarterlies.`index` = :quarterlyIndex")
    fun getInfo(quarterlyIndex: String): QuarterlyInfoEntity?

    @Transaction
    @Query("SELECT * FROM quarterlies WHERE quarterlies.`index` = :quarterlyIndex")
    fun getInfoFlow(quarterlyIndex: String): Flow<QuarterlyInfoEntity?>

    @Query("SELECT cover FROM quarterlies WHERE lang = :language")
    suspend fun getCovers(language: String): List<String>

    @Query("SELECT offlineState FROM quarterlies WHERE 'index' = :index")
    suspend fun getOfflineState(index: String): OfflineState?

    @Query("UPDATE quarterlies SET offlineState = :state WHERE 'index' = :index")
    suspend fun setOfflineState(index: String, state: OfflineState)
}
