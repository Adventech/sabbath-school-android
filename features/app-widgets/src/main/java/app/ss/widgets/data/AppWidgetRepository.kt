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

package app.ss.widgets.data

import android.content.Context
import app.ss.models.AppWidgetDay
import app.ss.widgets.AppWidgetAction
import app.ss.widgets.model.TodayWidgetState
import app.ss.widgets.model.WeekDayModel
import app.ss.widgets.model.WeekModel
import app.ss.widgets.model.WeekWidgetState
import com.cryart.sabbathschool.core.extensions.context.fetchBitmap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import ss.foundation.coroutines.DispatcherProvider
import ss.lessons.api.helper.SyncHelper
import ss.lessons.api.repository.QuarterlyIndexRepository
import ss.libraries.storage.api.dao.AppWidgetDao
import ss.libraries.storage.api.entity.AppWidgetEntity
import ss.misc.DateHelper.formatDate
import ss.misc.DateHelper.parseDate
import ss.misc.SSConstants.SS_DATE_FORMAT_OUTPUT_DAY
import ss.misc.SSConstants.SS_DATE_FORMAT_OUTPUT_DAY_SHORT
import ss.prefs.api.SSPrefs
import timber.log.Timber
import javax.inject.Inject

interface AppWidgetRepository {

    fun weekState(context: Context? = null): Flow<WeekWidgetState>

    fun todayState(context: Context? = null): Flow<TodayWidgetState>

    suspend fun sync(skipCache: Boolean = false)
}

private const val MAX_DAYS = 7

class AppWidgetRepositoryImpl @Inject constructor(
    private val appWidgetDao: AppWidgetDao,
    private val ssPrefs: SSPrefs,
    private val widgetAction: AppWidgetAction,
    private val syncHelper: SyncHelper,
    private val quarterlyIndexRepository: QuarterlyIndexRepository,
    private val dispatcherProvider: DispatcherProvider,
) : AppWidgetRepository {

    private val today get() = DateTime.now().withTimeAtStartOfDay()

    private suspend fun defaultQuarterlyIndex(
        languageCode: String = ssPrefs.getLanguageCode()
    ): String = quarterlyIndexRepository(languageCode)

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun latestAppWidgetEntity(): Flow<AppWidgetEntity?> {
        return ssPrefs.getLanguageCodeFlow()
            .map { defaultQuarterlyIndex(it) }
            .flatMapLatest { appWidgetDao.findBy(it) }
            .flowOn(dispatcherProvider.io)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun weekState(context: Context?): Flow<WeekWidgetState> {
        return latestAppWidgetEntity()
            .mapLatest { entity ->
                entity?.let {
                    val image = withContext(dispatcherProvider.io) { context?.fetchBitmap(entity.cover) }
                    val days = entity.days
                        .take(MAX_DAYS)
                        .map { day ->
                            day.toModel(context, SS_DATE_FORMAT_OUTPUT_DAY_SHORT)
                        }.toImmutableList()
                    WeekWidgetState.Success(
                        model = WeekModel(
                            quarterlyIndex = entity.quarterlyIndex,
                            cover = entity.cover,
                            image = image,
                            title = entity.title,
                            description = entity.description,
                            days = days
                        ),
                        lessonIntent = widgetAction.launchLesson(entity.quarterlyIndex)
                    )
                } ?: WeekWidgetState.Error
            }
            .flowOn(dispatcherProvider.io)
            .catch { Timber.e(it) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun todayState(context: Context?): Flow<TodayWidgetState> {
        return latestAppWidgetEntity()
            .mapLatest { entity ->
                val day = entity?.days?.firstOrNull { it.isToday() }
                day?.let {
                    TodayWidgetState.Success(
                        model = it.toModel(context, SS_DATE_FORMAT_OUTPUT_DAY),
                        launchIntent = widgetAction.launchRead(
                            lessonIndex = it.lessonIndex,
                            dayIndex = it.dayIndex.toString()
                        )
                    )
                } ?: TodayWidgetState.Error
            }
            .flowOn(dispatcherProvider.io)
            .catch {
                Timber.e(it)
                emit(TodayWidgetState.Error)
            }
    }

    override suspend fun sync(skipCache: Boolean) {
        val quarterlyIndex = defaultQuarterlyIndex()
        syncHelper.syncAppWidgetInfo(quarterlyIndex, skipCache)
    }

    private suspend fun AppWidgetDay.toModel(context: Context?, dateFormat: String) = WeekDayModel(
        title = title,
        date = formatDate(date, dateFormat),
        cover = image,
        image = withContext(dispatcherProvider.io) { context?.fetchBitmap(image) },
        intent = widgetAction.launchRead(
            lessonIndex = lessonIndex,
            dayIndex = dayIndex.toString()
        ),
        today = isToday()
    )

    // Return true if the day is today
    private fun AppWidgetDay.isToday(): Boolean {
        return today.isEqual(parseDate(date))
    }

}
