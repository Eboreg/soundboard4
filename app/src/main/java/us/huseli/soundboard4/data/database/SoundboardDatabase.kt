package us.huseli.soundboard4.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import us.huseli.soundboard4.data.database.dao.CategoryDao
import us.huseli.soundboard4.data.database.dao.SoundDao
import us.huseli.soundboard4.data.database.model.Category
import us.huseli.soundboard4.data.database.model.Sound

@Database(
    entities = [
        Category::class,
        Sound::class,
    ],
    version = 7,
    exportSchema = false,
)
@TypeConverters(SoundboardTypeConverters::class)
abstract class SoundboardDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun soundDao(): SoundDao

    companion object {
        fun build(context: Context): SoundboardDatabase {
            val builder = Room
                .databaseBuilder(context.applicationContext, SoundboardDatabase::class.java, "db.sqlite3")
                .fallbackToDestructiveMigration(dropAllTables = true)

            return builder.build()
        }
    }
}
