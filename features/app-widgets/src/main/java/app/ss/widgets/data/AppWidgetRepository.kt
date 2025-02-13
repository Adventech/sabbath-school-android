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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import org.joda.time.DateTime
import org.joda.time.LocalDate
import ss.foundation.coroutines.DispatcherProvider
import ss.lessons.api.helper.SyncHelper
import ss.libraries.appwidget.api.AppWidgetHelper
import ss.libraries.storage.api.dao.AppWidgetDao
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

    suspend fun sync()
}

private const val MAX_DAYS = 7

class AppWidgetRepositoryImpl @Inject constructor(
    private val appWidgetDao: AppWidgetDao,
    private val ssPrefs: SSPrefs,
    private val widgetAction: AppWidgetAction,
    private val syncHelper: SyncHelper,
    private val helper: AppWidgetHelper,
    private val dispatcherProvider: DispatcherProvider,
) : AppWidgetRepository {

    private val today get() = DateTime.now().withTimeAtStartOfDay()

    private fun defaultQuarterlyIndex(): String {
        val languageCode = ssPrefs.getLanguageCode()
        val currentDate = LocalDate.now()
        val year = currentDate.year
        val quarter = when (currentDate.monthOfYear) {
            in 1..3 -> "01"
            in 4..6 -> "02"
            in 7..9 -> "03"
            else -> "04"
        }
        return "$languageCode-$year-$quarter"
    }

    override fun weekState(context: Context?): Flow<WeekWidgetState> {
        return appWidgetDao.findBy(defaultQuarterlyIndex())
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
                                day.toModel(null, SS_DATE_FORMAT_OUTPUT_DAY_SHORT)
                            }.toImmutableList()
                    ),
                    lessonIntent = widgetAction.launchLesson(entity.quarterlyIndex)
                )
            }
            .onStart { sync() }
            .flowOn(dispatcherProvider.io)
            .catch {
                Timber.e(it)
            }
    }

    override fun todayState(context: Context?): Flow<TodayWidgetState> {
        return appWidgetDao.findBy(defaultQuarterlyIndex())
            .filterNotNull()
            .map { entity ->
                val day = entity.days.firstOrNull { it.isToday() }
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
            .onStart { sync() }
            .flowOn(dispatcherProvider.io)
            .catch {
                Timber.e(it)
                emit(TodayWidgetState.Error)
            }
    }

    override suspend fun sync() {
        val quarterlyIndex = defaultQuarterlyIndex()
        syncHelper.syncQuarterlyInfo(quarterlyIndex)
        helper.syncQuarterly(quarterlyIndex)
    }

    private suspend fun AppWidgetDay.toModel(context: Context?, dateFormat: String) = WeekDayModel(
        title = title,
        date = formatDate(date, dateFormat),
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
