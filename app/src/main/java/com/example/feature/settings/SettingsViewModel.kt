package com.example.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppPreferences
import com.example.domain.model.Playlist
import com.example.domain.repository.IChannelRepository
import com.example.domain.usecase.ManagePlaylistUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Scoped ViewModel for SettingsScreen managing cache clearing, playback history, and preferences.
 */
class SettingsViewModel(
    private val channelRepository: IChannelRepository,
    private val managePlaylistUseCase: ManagePlaylistUseCase,
    private val preferences: AppPreferences
) : ViewModel() {

    private val _activePlaylistId = MutableStateFlow<Long?>(null)
    val activePlaylistId: StateFlow<Long?> = _activePlaylistId.asStateFlow()

    private val _showClearHistoryDialog = MutableStateFlow(false)
    val showClearHistoryDialog: StateFlow<Boolean> = _showClearHistoryDialog.asStateFlow()

    private val _showClearCacheDialog = MutableStateFlow(false)
    val showClearCacheDialog: StateFlow<Boolean> = _showClearCacheDialog.asStateFlow()

    val playlists: StateFlow<List<Playlist>> = managePlaylistUseCase.allPlaylists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            preferences.activePlaylistId.collect { id ->
                _activePlaylistId.value = id
            }
        }
    }

    fun openClearHistoryDialog() {
        _showClearHistoryDialog.value = true
    }

    fun closeClearHistoryDialog() {
        _showClearHistoryDialog.value = false
    }

    fun openClearCacheDialog() {
        _showClearCacheDialog.value = true
    }

    fun closeClearCacheDialog() {
        _showClearCacheDialog.value = false
    }

    fun clearPlaybackHistory() {
        viewModelScope.launch {
            channelRepository.clearRecentHistory()
            _showClearHistoryDialog.value = false
        }
    }

    fun clearChannelCache() {
        viewModelScope.launch {
            managePlaylistUseCase.clearCache()
            _showClearCacheDialog.value = false
        }
    }

    fun selectPlaylist(playlistId: Long) {
        viewModelScope.launch {
            _activePlaylistId.value = playlistId
            preferences.setActivePlaylistId(playlistId)
            managePlaylistUseCase.updateLastUsed(playlistId)
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            managePlaylistUseCase.delete(playlistId)
        }
    }
}
