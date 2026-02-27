package com.callarchive.app.di

import com.callarchive.app.data.repository.CallRecordingRepositoryImpl
import com.callarchive.app.data.repository.DriveRepositoryImpl
import com.callarchive.app.data.repository.TranscriptionRepositoryImpl
import com.callarchive.app.domain.repository.CallRecordingRepository
import com.callarchive.app.domain.repository.DriveRepository
import com.callarchive.app.domain.repository.TranscriptionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindCallRecordingRepository(
        impl: CallRecordingRepositoryImpl
    ): CallRecordingRepository

    @Binds
    @Singleton
    abstract fun bindDriveRepository(
        impl: DriveRepositoryImpl
    ): DriveRepository

    @Binds
    @Singleton
    abstract fun bindTranscriptionRepository(
        impl: TranscriptionRepositoryImpl
    ): TranscriptionRepository
}
