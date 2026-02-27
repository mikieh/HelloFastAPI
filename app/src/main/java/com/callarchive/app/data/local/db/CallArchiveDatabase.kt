package com.callarchive.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [CallRecordingEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class CallArchiveDatabase : RoomDatabase() {
    abstract fun callRecordingDao(): CallRecordingDao
}
