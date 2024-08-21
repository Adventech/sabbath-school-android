package ss.libraries.storage.api.dao

import androidx.room.Dao
import androidx.room.Query
import app.ss.models.AppWidgetDay
import kotlinx.coroutines.flow.Flow
import ss.libraries.storage.api.entity.AppWidgetEntity

@Dao
interface AppWidgetDao: BaseDao<AppWidgetEntity> {

    @Query("SELECT * FROM app_widget WHERE quarterlyIndex = :quarterlyIndex")
    fun findBy(quarterlyIndex: String): Flow<AppWidgetEntity?>

    @Query("UPDATE app_widget SET days = :days WHERE quarterlyIndex = :quarterlyIndex")
    suspend fun updateDays(quarterlyIndex: String, days: List<AppWidgetDay>)
}
