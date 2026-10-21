package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaybackHistoryDao {
    @Query("SELECT * FROM playback_history ORDER BY timestamp DESC LIMIT 5")
    fun getRecentChannels(): Flow<List<PlaybackHistoryEntity>>

    @Query("SELECT * FROM playback_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentChannelsWithLimit(limit: Int): Flow<List<PlaybackHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordHistory(history: PlaybackHistoryEntity)

    @Query("DELETE FROM playback_history WHERE channelId = :channelId OR streamUrl = :streamUrl")
    suspend fun deleteByChannelOrUrl(channelId: Long, streamUrl: String)

    @Query("DELETE FROM playback_history WHERE channelId = :channelId")
    suspend fun deleteByChannelId(channelId: Long)

    @Query("DELETE FROM playback_history")
    suspend fun clearHistory()

    @Query("SELECT COUNT(*) FROM playback_history")
    suspend fun getHistoryCount(): Int
}
