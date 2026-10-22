package com.example.domain.usecase

import com.example.domain.model.ImportStatus
import com.example.domain.model.Playlist
import com.example.domain.repository.IPlaylistRepository
import kotlinx.coroutines.flow.Flow

/**
 * Encapsulates playlist lifecycle and management operations (rename, delete, refresh, cache clearing).
 */
class ManagePlaylistUseCase(
    private val playlistRepository: IPlaylistRepository
) {
    val allPlaylists: Flow<List<Playlist>> = playlistRepository.allPlaylists

    suspend fun rename(playlistId: Long, newName: String) {
        playlistRepository.renamePlaylist(playlistId, newName)
    }

    suspend fun delete(playlistId: Long): Result<Unit> {
        return playlistRepository.deletePlaylist(playlistId)
    }

    suspend fun refresh(playlistId: Long): ImportStatus {
        return playlistRepository.refreshPlaylist(playlistId)
    }

    suspend fun updateLastUsed(playlistId: Long) {
        playlistRepository.updateLastUsed(playlistId)
    }

    suspend fun clearCache(): Result<Unit> {
        return playlistRepository.clearChannelCache()
    }
}
