package app.ss.storage.test

import ss.libraries.storage.api.dao.AppWidgetDao
import app.ss.models.AppWidgetDay
import ss.libraries.storage.api.entity.AppWidgetEntity

class FakeAppWidgetDao : AppWidgetDao {
    override suspend fun findBy(quarterlyIndex: String): AppWidgetEntity? {
        return null
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
