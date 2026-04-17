package com.example.luontopeli.data.local

import android.content.Context
import com.example.luontopeli.data.local.dao.NatureSpotDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideNatureSpotDao(
        db: AppDatabase
    ): NatureSpotDao {
        return db.natureSpotDao()
    }
}