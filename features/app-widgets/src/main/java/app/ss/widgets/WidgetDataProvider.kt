package app.ss.widgets

import app.ss.lessons.data.repository.lessons.LessonsRepository
import app.ss.widgets.model.TodayWidgetModel
import app.ss.widgets.model.WeekDayWidgetModel
import app.ss.widgets.model.WeekLessonWidgetModel
import com.cryart.sabbathschool.core.extensions.coroutines.DispatcherProvider
import com.cryart.sabbathschool.core.navigation.Destination
import com.cryart.sabbathschool.core.navigation.toUri
import kotlinx.coroutines.withContext
import ss.misc.SSConstants
import timber.log.Timber
import javax.inject.Inject

internal interface WidgetDataProvider {

    suspend fun getTodayModel(): TodayWidgetModel?

    suspend fun getWeekLessonModel(): WeekLessonWidgetModel?

    suspend fun sync()
}

internal class WidgetDataProviderImpl @Inject constructor(
    private val repository: LessonsRepository,
    private val dispatcherProvider: DispatcherProvider
) : WidgetDataProvider {

    override suspend fun getTodayModel(): TodayWidgetModel? = fetchTodayModel(cached = true)

    private suspend fun fetchTodayModel(cached: Boolean) = withContext(dispatcherProvider.default) {
        try {
            repository.getTodayRead(cached).data?.let { data ->
                TodayWidgetModel(
                    data.title,
                    data.date,
                    data.cover,
                    Destination.READ.toUri(SSConstants.SS_LESSON_INDEX_EXTRA to data.lessonIndex)
                )
            }
        } catch (ex: Exception) {
            Timber.e(ex)
            null
        }
    }

    override suspend fun getWeekLessonModel(): WeekLessonWidgetModel? = fetchWeekLessonModel(cached = true)

    private suspend fun fetchWeekLessonModel(cached: Boolean) = withContext(dispatcherProvider.default) {
        try {
            repository.getWeekData(cached = cached).data?.let { data ->
                val days = data.days.mapIndexed { index, day ->
                    WeekDayWidgetModel(
                        day.title,
                        day.date,
                        Destination.READ.toUri(
                            SSConstants.SS_LESSON_INDEX_EXTRA to data.lessonIndex,
                            SSConstants.SS_READ_POSITION_EXTRA to index.toString()
                        ),
                        day.today
                    )
                }

                WeekLessonWidgetModel(
                    data.quarterlyTitle,
                    data.lessonTitle,
                    data.cover,
                    days,
                    Destination.LESSONS.toUri(
                        SSConstants.SS_QUARTERLY_INDEX_EXTRA to data.quarterlyIndex
                    )
                )
            }
        } catch (ex: Exception) {
            Timber.e(ex)
            null
        }
    }

    override suspend fun sync() {
        fetchTodayModel(cached = false)
        fetchWeekLessonModel(cached = false)
    }
}
