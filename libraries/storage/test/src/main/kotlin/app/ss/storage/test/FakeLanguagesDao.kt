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

package app.ss.storage.test

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import ss.libraries.storage.api.dao.LanguagesDao
import ss.libraries.storage.api.entity.LanguageEntity

/**
 * Fake implementation of [LanguagesDao] for use in tests.
 */
class FakeLanguagesDao(
    private val languagesFlow: MutableStateFlow<List<LanguageEntity>> = MutableStateFlow(emptyList())
) : LanguagesDao {

    override fun get(): Flow<List<LanguageEntity>> = languagesFlow
    override fun get(code: String): Flow<LanguageEntity?> {
        return languagesFlow.map { languages -> languages.firstOrNull { it.code == code } }
    }

    override fun search(query: String): Flow<List<LanguageEntity>> = languagesFlow

    override suspend fun insertItem(item: LanguageEntity) {
        languagesFlow.update { it.toMutableList() + item }
    }

    override suspend fun insertAll(items: List<LanguageEntity>) {
        languagesFlow.update { items }
    }

    override suspend fun update(item: LanguageEntity) {}
}
