package us.huseli.soundboard4.data.database.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

interface BaseDao<T> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: T): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg entity: T)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: Collection<T>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(entity: T)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateAll(vararg entity: T)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateAll(entities: Collection<T>)

    @Delete
    suspend fun delete(entity: T): Int

    @Delete
    suspend fun deleteAll(entities: Collection<T>)
}
