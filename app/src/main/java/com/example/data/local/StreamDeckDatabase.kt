package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        PlaylistEntity::class,
        ChannelEntity::class,
        PlaybackHistoryEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class StreamDeckDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
    abstract fun channelDao(): ChannelDao
    abstract fun playbackHistoryDao(): PlaybackHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: StreamDeckDatabase? = null

        fun getInstance(context: Context): StreamDeckDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StreamDeckDatabase::class.java,
                    "streamdeck_tv.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
