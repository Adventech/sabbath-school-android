package app.ss.widgets

import app.ss.lessons.data.repository.lessons.LessonsRepository
import app.ss.widgets.model.TodayWidgetModel
import timber.log.Timber

internal interface WidgetDataProvider {

    suspend fun getTodayModel(): TodayWidgetModel?
}

internal class WidgetDataProviderImpl constructor(
    private val repository: LessonsRepository,
) : WidgetDataProvider {

    override suspend fun getTodayModel(): TodayWidgetModel? {
        return try {
            val model = repository.getTodayRead().data
            model?.let {
                TodayWidgetModel(it.index, it.lessonIndex, it.title, it.date)
            }
        } catch (ex: Exception) {
            Timber.e(ex)
            null
        }
    }
}
