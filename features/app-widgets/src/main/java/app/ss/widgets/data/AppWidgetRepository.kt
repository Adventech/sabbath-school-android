package app.ss.widgets.data

import android.content.Context
import android.content.Intent
import app.ss.models.AppWidgetDay
import app.ss.models.SSLesson
import app.ss.models.SSQuarterlyInfo
import app.ss.widgets.WidgetDataProvider
import app.ss.widgets.glance.extensions.fallbackIntent
import app.ss.widgets.model.TodayModel
import app.ss.widgets.model.TodayWidgetState
import app.ss.widgets.model.WeekModel
import app.ss.widgets.model.WeekWidgetState
import com.cryart.sabbathschool.core.extensions.context.fetchBitmap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import ss.foundation.coroutines.DispatcherProvider
import ss.foundation.coroutines.Scopable
import ss.foundation.coroutines.ioScopable
import ss.libraries.storage.api.dao.AppWidgetDao
import ss.libraries.storage.api.dao.LessonsDao
import ss.libraries.storage.api.dao.QuarterliesDao
import ss.libraries.storage.api.entity.AppWidgetEntity
import ss.libraries.storage.api.entity.LessonEntity
import ss.misc.DateHelper
import ss.misc.DateHelper.formatDate
import ss.misc.DateHelper.isNowInRange
import ss.misc.DateHelper.parseDate
import ss.prefs.api.SSPrefs
import timber.log.Timber
import javax.inject.Inject

interface AppWidgetRepository {

    fun weekState(context: Context): Flow<WeekWidgetState>

    fun todayState(context: Context? = null): Flow<TodayWidgetState>
}

class AppWidgetRepositoryImpl @Inject constructor(
    private val appWidgetDao: AppWidgetDao,
    private val quarterliesDao: QuarterliesDao,
    private val ssPrefs: SSPrefs,
    private val dispatcherProvider: DispatcherProvider,
) : AppWidgetRepository, Scopable by ioScopable(dispatcherProvider) {

    private val today get() = DateTime.now().withTimeAtStartOfDay()

    private fun quarterlyIndex(): Flow<String?> = ssPrefs.lastQuarterlyIndex()
        .map { it ?: defaultQuarterlyIndex() }

    private suspend fun defaultQuarterlyIndex(): String? {
        val quarterlies = withContext(dispatcherProvider.io) {
            quarterliesDao.get(ssPrefs.getLanguageCode())
        }
        return quarterlies.firstOrNull { quarterly ->
            isNowInRange(quarterly.start_date, quarterly.end_date)
        }?.index
    }

    override fun weekState(context: Context): Flow<WeekWidgetState> {
        return quarterlyIndex()
            .filterNotNull()
            .flatMapLatest { appWidgetDao.findBy(it) }
            .filterNotNull()
            .map { entity ->
                WeekWidgetState.Success(
                    model = WeekModel(
                        quarterlyIndex = entity.quarterlyIndex,
                        cover = context.fetchBitmap(entity.cover),
                        title = entity.title,
                        description = entity.description,
                        days = entity.days.map { day ->
                            day.toModel(null)
                        }.toImmutableList()
                    ),
                    lessonIntent = fallbackIntent
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
                        launchIntent = fallbackIntent
                    )
                } ?: TodayWidgetState.Error
            }
            .flowOn(dispatcherProvider.io)
            .catch {
                Timber.e(it)
                emit(TodayWidgetState.Error)
            }
    }

    private suspend fun AppWidgetDay.toModel(context: Context?) = TodayModel(
        title = title,
        date = formatDate(date),
        image = context?.fetchBitmap(image),
    )

    // Return true if the day is today
    private fun AppWidgetDay.isToday(): Boolean {
        return today.isEqual(parseDate(date))
    }

}
