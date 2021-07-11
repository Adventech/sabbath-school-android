package app.ss.widgets

import app.ss.lessons.data.repository.lessons.LessonsRepository
import app.ss.widgets.model.TodayWidgetModel
import com.cryart.sabbathschool.core.extensions.logger.timber

internal interface WidgetDataProvider {

    suspend fun getTodayModel(): TodayWidgetModel?
}

internal class WidgetDataProviderImpl constructor(
    private val repository: LessonsRepository,
) : WidgetDataProvider {

    private val logger by timber()

    override suspend fun getTodayModel(): TodayWidgetModel? {
        return try {
            val response = repository.getTodayRead()
            logger.d("TodayModel: ${response.data}")

            response.data?.let { model ->
                TodayWidgetModel(model.index, model.lessonIndex, model.title, model.date, model.cover)
            }
        } catch (ex: Exception) {
            logger.e(ex)
            null
        }
    }
}
