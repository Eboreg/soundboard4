package us.huseli.soundboard4.data.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import us.huseli.soundboard4.data.database.SoundboardDatabase
import us.huseli.soundboard4.data.database.dao.CategoryDao
import us.huseli.soundboard4.data.database.dao.SoundDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataDiModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SoundboardDatabase =
        SoundboardDatabase.build(context)

    @Provides
    @Singleton
    fun provideCategoryDao(database: SoundboardDatabase): CategoryDao = database.categoryDao()

    @Provides
    @Singleton
    fun provideSoundDao(database: SoundboardDatabase): SoundDao = database.soundDao()
}
