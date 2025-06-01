package us.huseli.soundboard4.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import us.huseli.soundboard4.data.database.model.Category
import us.huseli.soundboard4.data.database.model.Sound

@Dao
abstract class CategoryDao : BaseDao<Category> {
    @Query("DELETE FROM categories")
    abstract suspend fun deleteAll()

    @Query("SELECT * FROM categories ORDER BY position")
    abstract fun flowAll(): Flow<List<Category>>

    @Query(
        """
        SELECT * FROM categories LEFT JOIN sounds ON categories.id = sounds.categoryId
        ORDER BY categories.position
        """
    )
    abstract fun flowAllMultimaps(): Flow<Map<Category, List<Sound>>>

    @Query("SELECT * FROM categories WHERE id = :categoryId")
    abstract fun flowOne(categoryId: String): Flow<Category?>

    @Query("SELECT * FROM categories WHERE id = :id")
    abstract suspend fun get(id: String): Category?

    @Query("SELECT MAX(position) FROM categories")
    abstract suspend fun getHighestPosition(): Int?

    @Query("SELECT * FROM categories WHERE position > :position ORDER BY position LIMIT 1")
    abstract suspend fun getNext(position: Int): Category?

    @Query("SELECT * FROM categories WHERE position < :position ORDER BY position DESC LIMIT 1")
    abstract suspend fun getPrevious(position: Int): Category?

    @Query("SELECT * FROM categories ORDER BY position")
    abstract suspend fun listAll(): List<Category>

    @Query("UPDATE categories SET isCollapsed = :collapsed WHERE id = :categoryId")
    abstract suspend fun setCollapsed(categoryId: String, collapsed: Boolean)
}
