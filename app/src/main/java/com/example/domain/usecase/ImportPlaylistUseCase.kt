package com.example.domain.usecase

import android.net.Uri
import com.example.domain.model.ImportStatus
import com.example.domain.repository.IPlaylistRepository

/**
 * Orchestrates playlist import workflows (URL, local file, demo channels, default catalog).
 */
class ImportPlaylistUseCase(
    private val playlistRepository: IPlaylistRepository
) {
    suspend fun fromUrl(url: String, customName: String? = null): ImportStatus {
        return playlistRepository.importFromUrl(url, customName)
    }

    suspend fun fromUri(uri: Uri, customName: String? = null): ImportStatus {
        return playlistRepository.importFromUri(uri, customName)
    }

    suspend fun loadDemo(): ImportStatus {
        return playlistRepository.loadDemoChannels()
    }

    suspend fun ensureDefault(forceRefresh: Boolean = false): ImportStatus {
        return playlistRepository.ensureDefaultPlaylistLoaded(forceRefresh)
    }
}
