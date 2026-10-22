package com.example.feature.playlists

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppPreferences
import com.example.domain.model.ImportStatus
import com.example.domain.model.Playlist
import com.example.domain.usecase.ImportPlaylistUseCase
import com.example.domain.usecase.ManagePlaylistUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Scoped ViewModel for PlaylistsScreen and AddPlaylistDialog managing playlist CRUD and imports.
 */
class PlaylistsViewModel(
    private val managePlaylistUseCase: ManagePlaylistUseCase,
    private val importPlaylistUseCase: ImportPlaylistUseCase,
    private val preferences: AppPreferences
) : ViewModel() {

    private val _activePlaylistId = MutableStateFlow<Long?>(null)
    val activePlaylistId: StateFlow<Long?> = _activePlaylistId.asStateFlow()

    private val _refreshingPlaylistId = MutableStateFlow<Long?>(null)
    val refreshingPlaylistId: StateFlow<Long?> = _refreshingPlaylistId.asStateFlow()

    private val _showAddPlaylistDialog = MutableStateFlow(false)
    val showAddPlaylistDialog: StateFlow<Boolean> = _showAddPlaylistDialog.asStateFlow()

    private val _importStatus = MutableStateFlow<ImportStatus>(ImportStatus.Idle)
    val importStatus: StateFlow<ImportStatus> = _importStatus.asStateFlow()

    val playlists: StateFlow<List<Playlist>> = managePlaylistUseCase.allPlaylists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            preferences.activePlaylistId.collect { id ->
                _activePlaylistId.value = id
            }
        }
    }

    fun openAddPlaylistDialog() {
        _showAddPlaylistDialog.value = true
    }

    fun closeAddPlaylistDialog() {
        _showAddPlaylistDialog.value = false
        clearImportStatus()
    }

    fun clearImportStatus() {
        _importStatus.value = ImportStatus.Idle
    }

    fun selectPlaylist(playlistId: Long) {
        viewModelScope.launch {
            _activePlaylistId.value = playlistId
            preferences.setActivePlaylistId(playlistId)
            managePlaylistUseCase.updateLastUsed(playlistId)
        }
    }

    fun renamePlaylist(playlistId: Long, newName: String) {
        viewModelScope.launch {
            managePlaylistUseCase.rename(playlistId, newName)
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            managePlaylistUseCase.delete(playlistId)
        }
    }

    fun refreshPlaylist(playlistId: Long) {
        viewModelScope.launch {
            _refreshingPlaylistId.value = playlistId
            _importStatus.value = ImportStatus.Loading("Refreshing playlist channels…")
            val result = managePlaylistUseCase.refresh(playlistId)
            _importStatus.value = result
            _refreshingPlaylistId.value = null
        }
    }

    fun importFromUrl(url: String, customName: String? = null) {
        viewModelScope.launch {
            _importStatus.value = ImportStatus.Loading("Validating and downloading playlist from URL…")
            val result = importPlaylistUseCase.fromUrl(url, customName)
            _importStatus.value = result
            if (result is ImportStatus.Success) {
                selectPlaylist(result.playlist.id)
            }
        }
    }

    fun importFromUri(uri: Uri, customName: String? = null) {
        viewModelScope.launch {
            _importStatus.value = ImportStatus.Loading("Reading local M3U file…")
            val result = importPlaylistUseCase.fromUri(uri, customName)
            _importStatus.value = result
            if (result is ImportStatus.Success) {
                selectPlaylist(result.playlist.id)
            }
        }
    }

    fun loadDemoChannels() {
        viewModelScope.launch {
            _importStatus.value = ImportStatus.Loading("Loading demo channels…")
            val result = importPlaylistUseCase.loadDemo()
            _importStatus.value = result
            if (result is ImportStatus.Success) {
                selectPlaylist(result.playlist.id)
            }
        }
    }
}
