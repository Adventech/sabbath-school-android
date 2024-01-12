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

package app.ss.lessons.data.repository.quarterly

import app.ss.lessons.data.repository.DataSource
import app.ss.lessons.data.repository.DataSourceMediator
import app.ss.lessons.data.repository.LocalDataSource
import app.ss.models.LessonPdf
import app.ss.models.OfflineState
import app.ss.models.SSDay
import app.ss.models.SSLesson
import app.ss.models.SSQuarterly
import app.ss.models.SSQuarterlyInfo
import com.cryart.sabbathschool.core.response.Resource
import ss.foundation.android.connectivity.ConnectivityHelper
import ss.foundation.coroutines.DispatcherProvider
import ss.lessons.api.SSQuarterliesApi
import ss.libraries.storage.api.dao.LessonsDao
import ss.libraries.storage.api.dao.QuarterliesDao
import ss.libraries.storage.api.entity.LessonEntity
import ss.libraries.storage.api.entity.QuarterlyEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class QuarterlyInfoDataSource @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    connectivityHelper: ConnectivityHelper,
    private val quarterliesApi: SSQuarterliesApi,
    private val quarterliesDao: QuarterliesDao,
    private val lessonsDao: LessonsDao
) : DataSourceMediator<SSQuarterlyInfo, QuarterlyInfoDataSource.Request>(
    dispatcherProvider = dispatcherProvider,
    connectivityHelper = connectivityHelper
) {

    data class Request(val index: String)

    override val cache: LocalDataSource<SSQuarterlyInfo, Request> = object : LocalDataSource<SSQuarterlyInfo, Request> {
        override suspend fun getItem(request: Request): Resource<SSQuarterlyInfo> {
            val info = quarterliesDao.getInfo(request.index) ?: return Resource.loading()

            return Resource.success(
                SSQuarterlyInfo(
                    quarterly = info.quarterly.toModel(),
                    lessons = info.lessons.sortedBy { it.id }.map { it.toModel() }
                )
            )
        }

        override suspend fun updateItem(data: SSQuarterlyInfo) {
            data.lessons.forEach { lesson ->
                lessonsDao.get(lesson.index)?.let { entity ->
                    lessonsDao.update(lesson.toEntity(entity.days, entity.pdfs))
                } ?: run { lessonsDao.insertItem(lesson.toEntity()) }
            }
            val state = quarterliesDao.getOfflineState(data.quarterly.index) ?: OfflineState.NONE
            quarterliesDao.update(data.quarterly.toEntity(state))
        }
    }

    override val network: DataSource<SSQuarterlyInfo, Request> = object : DataSource<SSQuarterlyInfo, Request> {
        override suspend fun getItem(request: Request): Resource<SSQuarterlyInfo> {
            val error = Resource.error<SSQuarterlyInfo>(IllegalArgumentException("Required Quarterly [index]"))
            val index = request.index

            val language = index.substringBefore('-')
            val id = index.substringAfter('-')

            val response = quarterliesApi.getQuarterlyInfo(language, id)

            return response.body()?.let {
                val state = quarterliesDao.getOfflineState(it.quarterly.index) ?: OfflineState.NONE
                Resource.success(it.copy(quarterly = it.quarterly.copy(offlineState = state)))
            } ?: error
        }
    }

    private fun SSLesson.toEntity(
        days: List<SSDay> = emptyList(),
        pdfs: List<LessonPdf> = emptyList()
    ): LessonEntity = LessonEntity(
        index = index,
        quarter = index.substringBeforeLast('-'),
        title = title,
        start_date = start_date,
        end_date = end_date,
        cover = cover,
        id = id,
        path = path,
        full_path = full_path,
        pdfOnly = pdfOnly,
        days = days,
        pdfs = pdfs,
    )

    private fun LessonEntity.toModel(): SSLesson = SSLesson(
        index = index,
        title = title,
        start_date = start_date,
        end_date = end_date,
        cover = cover,
        id = id,
        path = path,
        full_path = full_path,
        pdfOnly = pdfOnly
    )

    internal fun QuarterlyEntity.toModel(): SSQuarterly = SSQuarterly(
        id = id,
        title = title,
        description = description,
        introduction = introduction,
        human_date = human_date,
        start_date = start_date,
        end_date = end_date,
        cover = cover,
        splash = splash,
        index = index,
        path = path,
        full_path = full_path,
        lang = lang,
        color_primary = color_primary,
        color_primary_dark = color_primary_dark,
        quarterly_name = quarterly_name,
        quarterly_group = quarterly_group,
        features = features,
        credits = credits,
        offlineState = offlineState
    )

    internal fun SSQuarterly.toEntity(
        offlineState: OfflineState
    ): QuarterlyEntity = QuarterlyEntity(
        index = index,
        id = id,
        title = title,
        description = description,
        introduction = introduction,
        human_date = human_date,
        start_date = start_date,
        end_date = end_date,
        cover = cover,
        splash = splash,
        path = path,
        full_path = full_path,
        lang = lang,
        color_primary = color_primary,
        color_primary_dark = color_primary_dark,
        quarterly_name = quarterly_name,
        quarterly_group = quarterly_group,
        features = features,
        credits = credits,
        offlineState = offlineState,
    )
}
