package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY isDefault DESC, lastUsed DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE id = :id LIMIT 1")
    suspend fun getPlaylistById(id: Long): PlaylistEntity?

    @Query("SELECT * FROM playlists WHERE sourceUri = :uri LIMIT 1")
    suspend fun getPlaylistByUri(uri: String): PlaylistEntity?

    @Query("SELECT * FROM playlists WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultPlaylist(): PlaylistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Query("UPDATE playlists SET name = :newName WHERE id = :id")
    suspend fun renamePlaylist(id: Long, newName: String)

    @Query("UPDATE playlists SET channelCount = :count, lastRefreshed = :timestamp WHERE id = :id")
    suspend fun updatePlaylistRefresh(id: Long, count: Int, timestamp: Long)

    @Query("UPDATE playlists SET lastUsed = :timestamp WHERE id = :id")
    suspend fun updateLastUsed(id: Long, timestamp: Long)

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylistById(id: Long)

    @Query("SELECT COUNT(*) FROM playlists")
    suspend fun getPlaylistCount(): Int
}
