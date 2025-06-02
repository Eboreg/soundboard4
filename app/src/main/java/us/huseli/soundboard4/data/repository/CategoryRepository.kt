package us.huseli.soundboard4.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import us.huseli.soundboard4.R
import us.huseli.soundboard4.data.database.dao.CategoryDao
import us.huseli.soundboard4.data.database.model.Category
import us.huseli.soundboard4.data.database.model.Sound
import us.huseli.soundboard4.randomColor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao,
    @ApplicationContext private val context: Context,
) {
    suspend fun delete(category: Category) = categoryDao.delete(category)

    fun flowAll(): Flow<List<Category>> = categoryDao.flowAll()

    fun flowAllMultimaps(): Flow<Map<Category, List<Sound>>> = categoryDao.flowAllMultimaps()

    fun flowOne(categoryId: String): Flow<Category?> = categoryDao.flowOne(categoryId)

    suspend fun insert(category: Category): Long {
        val position = categoryDao.getHighestPosition()?.plus(1) ?: 0
        return categoryDao.insert(category.copy(position = position))
    }

    suspend fun insertDefault() {
        if (categoryDao.listAll().isEmpty()) {
            categoryDao.insert(Category(name = context.getString(R.string.default_), backgroundColor = randomColor()))
        }
    }

    suspend fun listAll(): List<Category> = categoryDao.listAll()

    suspend fun moveDown(categoryId: String) {
        val category = categoryDao.get(categoryId) ?: return

        categoryDao.getNext(category.position)?.also { next ->
            categoryDao.updateAll(
                category.copy(position = next.position),
                next.copy(position = category.position),
            )
        }
    }

    suspend fun moveUp(categoryId: String) {
        val category = categoryDao.get(categoryId) ?: return

        categoryDao.getPrevious(category.position)?.also { previous ->
            categoryDao.updateAll(
                category.copy(position = previous.position),
                previous.copy(position = category.position),
            )
        }
    }

    suspend fun setCollapsed(categoryId: String, collapsed: Boolean) = categoryDao.setCollapsed(categoryId, collapsed)

    suspend fun update(category: Category) = categoryDao.update(category)
}
