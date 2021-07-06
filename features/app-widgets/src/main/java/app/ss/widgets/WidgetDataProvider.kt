package app.ss.widgets

import app.ss.lessons.data.model.TodayModel
import app.ss.lessons.data.repository.lessons.LessonsRepository
import timber.log.Timber

interface WidgetDataProvider {

    suspend fun getTodayModel(): TodayModel?
}

internal class WidgetDataProviderImpl constructor(
    private val repository: LessonsRepository,
) : WidgetDataProvider {

    override suspend fun getTodayModel(): TodayModel? {
        return try {
            repository.getTodayRead().data
        } catch (ex: Exception) {
            Timber.e(ex)
            null
        }
    }
}
