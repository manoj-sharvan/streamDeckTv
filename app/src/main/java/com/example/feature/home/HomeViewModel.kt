package com.example.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppPreferences
import com.example.data.parser.LanguageClassifier
import com.example.domain.model.Channel
import com.example.domain.model.PlaybackHistory
import com.example.domain.model.Playlist
import com.example.domain.repository.IChannelRepository
import com.example.domain.repository.IPlaylistRepository
import com.example.domain.usecase.ManagePlaylistUseCase
import com.example.domain.usecase.PlayChannelUseCase
import com.example.domain.usecase.ToggleFavoriteUseCase
import com.example.util.NetworkMonitor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Scoped ViewModel for HomeScreen providing dashboard feed, channels, favorites, and quick actions.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val playlistRepository: IPlaylistRepository,
    private val channelRepository: IChannelRepository,
    private val preferences: AppPreferences,
    private val playChannelUseCase: PlayChannelUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val managePlaylistUseCase: ManagePlaylistUseCase,
    networkMonitor: NetworkMonitor
) : ViewModel() {

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline

    private val _activePlaylistId = MutableStateFlow<Long?>(null)
    val activePlaylistId: StateFlow<Long?> = _activePlaylistId.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val playlists: StateFlow<List<Playlist>> = playlistRepository.allPlaylists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activePlaylist: StateFlow<Playlist?> = _activePlaylistId.flatMapLatest { id ->
        if (id != null) {
            playlistRepository.allPlaylists.flatMapLatest { list ->
                flowOf(list.firstOrNull { it.id == id })
            }
        } else {
            flowOf(null)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val channels: StateFlow<List<Channel>> = _activePlaylistId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else channelRepository.getChannelsByPlaylist(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentChannels: StateFlow<List<PlaybackHistory>> = channelRepository.recentChannels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favorites: StateFlow<List<Channel>> = _activePlaylistId.flatMapLatest { id ->
        if (id == null) channelRepository.getAllFavorites() else channelRepository.getFavoritesByPlaylist(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val languages: StateFlow<List<String>> = channels.flatMapLatest { channelList ->
        val distinct = channelList.map { it.effectiveLanguage }
            .filter { it.isNotBlank() }
            .distinct()
            .sortedWith { a, b ->
                if (a.equals(LanguageClassifier.LANG_TAMIL, ignoreCase = true)) -1
                else if (b.equals(LanguageClassifier.LANG_TAMIL, ignoreCase = true)) 1
                else if (a.equals(LanguageClassifier.LANG_OTHER, ignoreCase = true)) 1
                else if (b.equals(LanguageClassifier.LANG_OTHER, ignoreCase = true)) -1
                else a.compareTo(b)
            }
        flowOf(listOf("All") + distinct)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("All"))

    val contentCategories: StateFlow<List<String>> = channels.flatMapLatest { channelList ->
        val distinct = channelList.map { it.contentCategory }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
        flowOf(listOf("All") + distinct)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("All"))

    init {
        viewModelScope.launch {
            preferences.activePlaylistId.collect { id ->
                _activePlaylistId.value = id
            }
        }
    }

    fun selectPlaylist(playlistId: Long) {
        viewModelScope.launch {
            _activePlaylistId.value = playlistId
            preferences.setActivePlaylistId(playlistId)
            managePlaylistUseCase.updateLastUsed(playlistId)
        }
    }

    fun toggleFavorite(channel: Channel) {
        viewModelScope.launch {
            toggleFavoriteUseCase(channel)
        }
    }

    fun playChannel(channel: Channel) {
        viewModelScope.launch {
            playChannelUseCase(channel)
        }
    }

    fun refreshActivePlaylist() {
        val id = _activePlaylistId.value ?: return
        viewModelScope.launch {
            _isRefreshing.value = true
            managePlaylistUseCase.refresh(id)
            _isRefreshing.value = false
        }
    }
}
