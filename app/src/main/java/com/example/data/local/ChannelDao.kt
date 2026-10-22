package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ChannelDao {
    @Query("SELECT * FROM channels WHERE playlistId = :playlistId ORDER BY orderIndex ASC")
    fun getChannelsByPlaylist(playlistId: Long): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE playlistId = :playlistId")
    suspend fun getChannelsListByPlaylist(playlistId: Long): List<ChannelEntity>

    @Query("SELECT * FROM channels WHERE playlistId = :playlistId AND groupTitle = :category ORDER BY orderIndex ASC")
    fun getChannelsByCategory(playlistId: Long, category: String): Flow<List<ChannelEntity>>

    @Query("SELECT DISTINCT groupTitle FROM channels WHERE playlistId = :playlistId ORDER BY groupTitle ASC")
    fun getCategoriesByPlaylist(playlistId: Long): Flow<List<String>>

    @Query("SELECT DISTINCT COALESCE(userLanguageOverride, language) FROM channels WHERE playlistId = :playlistId ORDER BY 1 ASC")
    fun getLanguagesByPlaylist(playlistId: Long): Flow<List<String>>

    @Query("SELECT DISTINCT contentCategory FROM channels WHERE playlistId = :playlistId ORDER BY contentCategory ASC")
    fun getContentCategoriesByPlaylist(playlistId: Long): Flow<List<String>>

    @Query("SELECT * FROM channels WHERE isFavorite = 1 ORDER BY name ASC")
    fun getAllFavoriteChannels(): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE playlistId = :playlistId AND isFavorite = 1 ORDER BY orderIndex ASC")
    fun getFavoritesByPlaylist(playlistId: Long): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE playlistId = :playlistId AND isFavorite = 1")
    suspend fun getFavoritesListByPlaylist(playlistId: Long): List<ChannelEntity>

    @Query("SELECT * FROM channels WHERE id = :id LIMIT 1")
    suspend fun getChannelById(id: Long): ChannelEntity?

    @Query("""
        SELECT * FROM channels 
        WHERE playlistId = :playlistId 
          AND (name LIKE '%' || :query || '%' OR groupTitle LIKE '%' || :query || '%' OR language LIKE '%' || :query || '%')
        ORDER BY orderIndex ASC
    """)
    fun searchChannels(playlistId: Long, query: String): Flow<List<ChannelEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<ChannelEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannel(channel: ChannelEntity): Long

    @Query("UPDATE channels SET isFavorite = :isFavorite WHERE id = :channelId")
    suspend fun setFavorite(channelId: Long, isFavorite: Boolean)

    @Query("UPDATE channels SET userLanguageOverride = :language WHERE id = :channelId")
    suspend fun updateChannelLanguageOverride(channelId: Long, language: String?)

    @Query("DELETE FROM channels WHERE playlistId = :playlistId")
    suspend fun deleteChannelsByPlaylist(playlistId: Long)

    @Query("DELETE FROM channels WHERE isLocalMedia = 0")
    suspend fun deleteAllCachedChannels()

    @Query("DELETE FROM channels WHERE isLocalMedia = 1")
    suspend fun deleteLocalMediaChannels()

    @Query("SELECT COUNT(*) FROM channels WHERE playlistId = :playlistId")
    suspend fun getChannelCountByPlaylist(playlistId: Long): Int

    @Transaction
    suspend fun replacePlaylistChannels(playlistId: Long, channels: List<ChannelEntity>) {
        deleteChannelsByPlaylist(playlistId)
        insertChannels(channels)
    }
}
