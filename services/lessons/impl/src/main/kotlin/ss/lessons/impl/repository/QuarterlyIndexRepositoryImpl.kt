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

import app.ss.models.SSQuarterlyIndex
import app.ss.network.NetworkResource
import app.ss.network.safeApiCall
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import org.joda.time.LocalDate
import ss.foundation.android.connectivity.ConnectivityHelper
import ss.foundation.coroutines.DispatcherProvider
import ss.lessons.api.SSQuarterliesApi
import ss.lessons.api.repository.QuarterlyIndexRepository
import ss.lessons.impl.ext.toEntity
import ss.lessons.impl.ext.toModel
import ss.libraries.storage.api.dao.QuarterlyIndexDao
import ss.misc.DateHelper
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class QuarterlyIndexRepositoryImpl @Inject constructor(
    private val connectivityHelper: ConnectivityHelper,
    private val quarterliesApi: SSQuarterliesApi,
    private val quarterlyIndexDao: QuarterlyIndexDao,
    private val dispatcherProvider: DispatcherProvider
) : QuarterlyIndexRepository {

    override suspend fun invoke(language: String): String {
        val cached = withContext(dispatcherProvider.io) { 
            quarterlyIndexDao.getAll(language).map { it.toModel() }
        }
        if (cached.isNotEmpty()) {
            val index = indexFromQuarterlies(cached)
            if (index != null) return index
        }
        
        val remote = withContext(dispatcherProvider.default) { fetchQuarterlies(language) }
        if (remote.isNotEmpty()) {
            val index = indexFromQuarterlies(cached)
            if (index != null) return index
        }

        return CurrentQuarterIndex(
            currentDate = LocalDate.now(),
            languageCode = language,
        )
    }

    private suspend fun fetchQuarterlies(language: String): List<SSQuarterlyIndex> {
        return when (val response = safeApiCall(connectivityHelper) { quarterliesApi.getQuarterlies(language) }) {
            is NetworkResource.Failure -> {
                Timber.e("Failed to fetch Quarterlies: isNetwork=${response.isNetworkError}, ${response.errorBody}")
                emptyList()
            }
            is NetworkResource.Success -> response.value.body().orEmpty().also { quarterlies ->
                withContext(dispatcherProvider.io) {
                    // Only save the latest quarterly (Adult)
                    quarterlies.firstOrNull()?.toEntity()?.let {
                        quarterlyIndexDao.insertItem(it)
                    }
                }
            }
        }
    }

    private fun indexFromQuarterlies(quarterlies: List<SSQuarterlyIndex>): String? {
        val now = DateTime.now().withTimeAtStartOfDay()
        for (quarterly in quarterlies) {
            val startDate = DateHelper.parseDate(quarterly.startDate) ?: continue
            val endDate = DateHelper.parseDate(quarterly.endDate) ?: continue
            if (now in startDate..endDate) {
                return quarterly.index
            }
        }

        return null
    }
}
