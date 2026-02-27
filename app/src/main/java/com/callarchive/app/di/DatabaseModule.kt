package com.callarchive.app.di

import android.content.Context
import androidx.room.Room
import com.callarchive.app.data.local.db.CallArchiveDatabase
import com.callarchive.app.data.local.db.CallRecordingDao
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
    fun provideDatabase(@ApplicationContext context: Context): CallArchiveDatabase {
        return Room.databaseBuilder(
            context,
            CallArchiveDatabase::class.java,
            "call_archive.db"
        ).build()
    }

    @Provides
    fun provideCallRecordingDao(database: CallArchiveDatabase): CallRecordingDao {
        return database.callRecordingDao()
    }
}
