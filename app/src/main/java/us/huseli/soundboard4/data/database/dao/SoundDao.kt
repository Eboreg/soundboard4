package us.huseli.soundboard4.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import us.huseli.soundboard4.data.database.model.Sound

@Dao
abstract class SoundDao : BaseDao<Sound> {
    @Query("SELECT * FROM sounds")
    abstract fun flowAll(): Flow<List<Sound>>

    @Query("SELECT * FROM sounds WHERE categoryId = :categoryId")
    abstract fun flowByCategoryId(categoryId: String): Flow<List<Sound>>

    @Query("UPDATE sounds SET playCount = playCount + 1 WHERE id = :id")
    abstract suspend fun increasePlayCount(id: String)

    @Query("SELECT * FROM sounds")
    abstract suspend fun listAll(): List<Sound>
}
