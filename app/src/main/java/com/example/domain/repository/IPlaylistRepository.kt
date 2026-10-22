package com.example.domain.repository

import android.net.Uri
import com.example.domain.model.ImportStatus
import com.example.domain.model.Playlist
import kotlinx.coroutines.flow.Flow

/**
 * Interface representing playlist data operations and import management.
 */
interface IPlaylistRepository {
    val allPlaylists: Flow<List<Playlist>>
    suspend fun getPlaylistById(id: Long): Playlist?
    suspend fun getDefaultPlaylist(): Playlist?
    suspend fun getPlaylistCount(): Int
    suspend fun updateLastUsed(playlistId: Long)
    suspend fun renamePlaylist(playlistId: Long, newName: String)
    suspend fun deletePlaylist(playlistId: Long): Result<Unit>
    suspend fun clearChannelCache(): Result<Unit>
    suspend fun ensureDefaultPlaylistLoaded(forceRefresh: Boolean = false): ImportStatus
    suspend fun importFromUrl(url: String, customName: String? = null): ImportStatus
    suspend fun refreshPlaylist(playlistId: Long): ImportStatus
    suspend fun importFromUri(uri: Uri, customName: String? = null): ImportStatus
    suspend fun loadDemoChannels(): ImportStatus
}
