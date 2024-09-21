package app.ss.widgets.data

import android.content.Context
import app.ss.models.AppWidgetDay
import app.ss.widgets.AppWidgetAction
import app.ss.widgets.model.WeekDayModel
import app.ss.widgets.model.TodayWidgetState
import app.ss.widgets.model.WeekModel
import app.ss.widgets.model.WeekWidgetState
import com.cryart.sabbathschool.core.extensions.context.fetchBitmap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import ss.foundation.coroutines.DispatcherProvider
import ss.libraries.storage.api.dao.AppWidgetDao
import ss.libraries.storage.api.dao.QuarterliesDao
import ss.misc.DateHelper.formatDate
import ss.misc.DateHelper.isNowInRange
import ss.misc.DateHelper.parseDate
import ss.misc.SSConstants.SS_DATE_FORMAT_OUTPUT_DAY_SHORT
import ss.prefs.api.SSPrefs
import timber.log.Timber
import javax.inject.Inject

interface AppWidgetRepository {

    suspend fun defaultQuarterlyIndex(): String?

    fun weekState(context: Context? = null): Flow<WeekWidgetState>

    fun todayState(context: Context? = null): Flow<TodayWidgetState>
}

private const val MAX_DAYS = 7

class AppWidgetRepositoryImpl @Inject constructor(
    private val appWidgetDao: AppWidgetDao,
    private val quarterliesDao: QuarterliesDao,
    private val ssPrefs: SSPrefs,
    private val widgetAction: AppWidgetAction,
    private val dispatcherProvider: DispatcherProvider,
) : AppWidgetRepository {

    private val today get() = DateTime.now().withTimeAtStartOfDay()

    private fun quarterlyIndex(): Flow<String?> = ssPrefs.lastQuarterlyIndex()
        .map { it ?: defaultQuarterlyIndex() }

    override suspend fun defaultQuarterlyIndex(): String? {
        ssPrefs.getLastQuarterlyIndex()?.let { return it }

        val quarterlies = withContext(dispatcherProvider.io) {
            quarterliesDao.get(ssPrefs.getLanguageCode())
        }
        return quarterlies.firstOrNull { quarterly ->
            isNowInRange(quarterly.start_date, quarterly.end_date)
        }?.index
    }

    override fun weekState(context: Context?): Flow<WeekWidgetState> {
        return quarterlyIndex()
            .filterNotNull()
            .flatMapLatest { appWidgetDao.findBy(it) }
            .filterNotNull()
            .map { entity ->
                WeekWidgetState.Success(
                    model = WeekModel(
                        quarterlyIndex = entity.quarterlyIndex,
                        cover = entity.cover,
                        image = context?.fetchBitmap(entity.cover),
                        title = entity.title,
                        description = entity.description,
                        days = entity.days
                            .take(MAX_DAYS)
                            .map { day ->
                                day.toModel(null)
                            }.toImmutableList()
                    ),
                    lessonIntent = widgetAction.launchLesson(entity.quarterlyIndex)
                )
            }
            .flowOn(dispatcherProvider.io)
            .catch {
                Timber.e(it)
            }
    }

    override fun todayState(context: Context?): Flow<TodayWidgetState> {
        return quarterlyIndex()
            .filterNotNull()
            .flatMapLatest { appWidgetDao.findBy(it) }
            .filterNotNull()
            .map { entity ->
                val day = entity.days.firstOrNull { it.isToday() }
                day?.let {
                    TodayWidgetState.Success(
                        model = it.toModel(context),
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

    private suspend fun AppWidgetDay.toModel(context: Context?) = WeekDayModel(
        title = title,
        date = formatDate(date, SS_DATE_FORMAT_OUTPUT_DAY_SHORT),
        cover = image,
        image = context?.fetchBitmap(image),
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
