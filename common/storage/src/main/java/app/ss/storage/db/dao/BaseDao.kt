package app.ss.storage.db.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

interface BaseDao<in T> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: T?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: T?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(items: List<T>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(item: T)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateItem(item: T)

    @Delete
    suspend fun delete(item: T)
}
