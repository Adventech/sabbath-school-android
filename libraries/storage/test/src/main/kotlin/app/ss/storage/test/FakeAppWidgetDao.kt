package app.ss.storage.test

import ss.libraries.storage.api.dao.AppWidgetDao
import app.ss.models.AppWidgetDay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import ss.libraries.storage.api.entity.AppWidgetEntity

class FakeAppWidgetDao : AppWidgetDao {
    override fun findBy(quarterlyIndex: String): Flow<AppWidgetEntity?> {
        return flowOf(null)
    }

    override suspend fun updateDays(quarterlyIndex: String, days: List<AppWidgetDay>) {
        // Do nothing
    }

    override suspend fun insertItem(item: AppWidgetEntity) {
        // Do nothing
    }

    override suspend fun insertAll(items: List<AppWidgetEntity>) {
        // Do nothing
    }

    override suspend fun update(item: AppWidgetEntity) {
        // Do nothing
    }
}
