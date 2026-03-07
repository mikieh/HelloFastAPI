package com.callarchive.app.di

import android.content.Context
import androidx.room.Room
import com.callarchive.app.data.local.db.CallFinderDatabase
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
    fun provideDatabase(@ApplicationContext context: Context): CallFinderDatabase {
        return Room.databaseBuilder(
            context,
            CallFinderDatabase::class.java,
            "call_archive.db"
        ).build()
    }

    @Provides
    fun provideCallRecordingDao(database: CallFinderDatabase): CallRecordingDao {
        return database.callRecordingDao()
    }
}
