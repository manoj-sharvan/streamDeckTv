package com.example

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppPreferences
import com.example.data.parser.LanguageClassifier
import com.example.data.repository.ChannelRepository
import com.example.data.repository.ImportStatus
import com.example.data.repository.PlaylistRepository
import com.example.domain.model.Channel
import com.example.domain.model.PlaybackHistory
import com.example.domain.model.Playlist
import com.example.player.PlayerUiState
import com.example.player.StreamPlayerManager
import com.example.util.NetworkMonitor
import com.example.util.SearchFilter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class NavTab {
    HOME,
    LIVE_TV,
    FAVORITES,
    PLAYLISTS,
    LOCAL_MEDIA,
    SETTINGS
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(
    private val playlistRepository: PlaylistRepository,
    private val channelRepository: ChannelRepository,
    private val preferences: AppPreferences,
    private val playerManager: StreamPlayerManager,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    // Predictable TV Navigation Back Stack (prevents duplicate screens, enables deep Back traversal)
    private val navStack = mutableListOf(NavTab.HOME)

    private val _currentTab = MutableStateFlow(NavTab.HOME)
    val currentTab: StateFlow<NavTab> = _currentTab.asStateFlow()

    private val _activePlaylistId = MutableStateFlow<Long?>(null)
    val activePlaylistId: StateFlow<Long?> = _activePlaylistId.asStateFlow()

    private val _selectedLanguage = MutableStateFlow<String?>("All")
    val selectedLanguage: StateFlow<String?> = _selectedLanguage.asStateFlow()

    private val _selectedContentCategory = MutableStateFlow<String?>("All")
    val selectedContentCategory: StateFlow<String?> = _selectedContentCategory.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>("All")
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _importStatus = MutableStateFlow<ImportStatus>(ImportStatus.Idle)
    val importStatus: StateFlow<ImportStatus> = _importStatus.asStateFlow()

    private val _refreshingPlaylistId = MutableStateFlow<Long?>(null)
    val refreshingPlaylistId: StateFlow<Long?> = _refreshingPlaylistId.asStateFlow()

    // Dialog & overlay states
    private val _channelForActions = MutableStateFlow<Channel?>(null)
    val channelForActions: StateFlow<Channel?> = _channelForActions.asStateFlow()

    private val _channelForLanguageEdit = MutableStateFlow<Channel?>(null)
    val channelForLanguageEdit: StateFlow<Channel?> = _channelForLanguageEdit.asStateFlow()

    private val _showAddPlaylistDialog = MutableStateFlow(false)
    val showAddPlaylistDialog: StateFlow<Boolean> = _showAddPlaylistDialog.asStateFlow()

    private val _activePlayingChannelId = MutableStateFlow<Long?>(null)
    val activePlayingChannelId: StateFlow<Long?> = _activePlayingChannelId.asStateFlow()

    private val _showExitDialog = MutableStateFlow(false)
    val showExitDialog: StateFlow<Boolean> = _showExitDialog.asStateFlow()

    private val _showClearHistoryDialog = MutableStateFlow(false)
    val showClearHistoryDialog: StateFlow<Boolean> = _showClearHistoryDialog.asStateFlow()

    private val _showClearCacheDialog = MutableStateFlow(false)
    val showClearCacheDialog: StateFlow<Boolean> = _showClearCacheDialog.asStateFlow()

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline

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
        if (id == null) {
            flowOf(emptyList())
        } else {
            channelRepository.getChannelsByPlaylist(id)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dynamic non-empty languages from actual channels in active playlist
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

    // Dynamic non-empty content categories (News, Sports, Movies, etc.)
    val contentCategories: StateFlow<List<String>> = channels.flatMapLatest { channelList ->
        val distinct = channelList.map { it.contentCategory }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
        flowOf(listOf("All") + distinct)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("All"))

    // Legacy group titles
    val categories: StateFlow<List<String>> = _activePlaylistId.flatMapLatest { id ->
        if (id != null) {
            channelRepository.getCategoriesByPlaylist(id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favorites: StateFlow<List<Channel>> = _activePlaylistId.flatMapLatest { id ->
        if (id == null) {
            channelRepository.getAllFavorites()
        } else {
            channelRepository.getFavoritesByPlaylist(id)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentChannels: StateFlow<List<PlaybackHistory>> = channelRepository.recentChannels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playerUiState: StateFlow<PlayerUiState> = playerManager.uiState

    init {
        // Step 1: Ensure Tamil TV default playlist is loaded and cached on first start
        viewModelScope.launch {
            val savedId = preferences.activePlaylistId.firstOrNull()
            val defaultResult = playlistRepository.ensureDefaultPlaylistLoaded(forceRefresh = false)

            if (savedId != null) {
                _activePlaylistId.value = savedId
            } else if (defaultResult is ImportStatus.Success) {
                _activePlaylistId.value = defaultResult.playlist.id
                preferences.setActivePlaylistId(defaultResult.playlist.id)
            }
        }

        // Step 2: Observe saved active playlist from DataStore preferences
        viewModelScope.launch {
            preferences.activePlaylistId.collect { savedId ->
                if (savedId != null) {
                    _activePlaylistId.value = savedId
                }
            }
        }

        // Step 3: Fallback if activePlaylistId is not set
        viewModelScope.launch {
            playlists.collect { list ->
                if (_activePlaylistId.value == null && list.isNotEmpty()) {
                    val defaultPl = list.firstOrNull { it.isDefault } ?: list.first()
                    _activePlaylistId.value = defaultPl.id
                    preferences.setActivePlaylistId(defaultPl.id)
                }
            }
        }

        // Step 4: Restore last played channel ID from preferences
        viewModelScope.launch {
            preferences.lastPlayedChannelId.collect { id ->
                if (id != null && _activePlayingChannelId.value == null) {
                    _activePlayingChannelId.value = id
                }
            }
        }
    }

    /**
     * Navigates to a tab while updating the predictable Android TV back stack.
     * Avoids consecutive duplicates of the same tab.
     */
    fun selectTab(tab: NavTab) {
        if (_currentTab.value != tab) {
            if (navStack.isEmpty() || navStack.last() != _currentTab.value) {
                navStack.add(_currentTab.value)
            }
            _currentTab.value = tab
        }
    }

    /**
     * Handles Android TV back press predictably:
     * 1. Closes player if active
     * 2. Dismisses open modal dialogs
     * 3. Clears search query or reset filter in Live TV before leaving
     * 4. Traverses backstack
     * 5. Displays Exit confirmation dialog if at Home tab
     * Returns true if handled internally, false if app should exit.
     */
    fun handleBackPress(): Boolean {
        // 1. Dismiss any open confirmation / channel / playlist dialogs first
        if (_showExitDialog.value) {
            _showExitDialog.value = false
            return true
        }
        if (_showAddPlaylistDialog.value) {
            dismissAddPlaylistDialog()
            return true
        }
        if (_channelForLanguageEdit.value != null) {
            _channelForLanguageEdit.value = null
            return true
        }
        if (_channelForActions.value != null) {
            _channelForActions.value = null
            return true
        }
        if (_showClearHistoryDialog.value) {
            _showClearHistoryDialog.value = false
            return true
        }
        if (_showClearCacheDialog.value) {
            _showClearCacheDialog.value = false
            return true
        }

        // 2. Close Video Player
        if (playerUiState.value.currentChannel != null) {
            closePlayer()
            return true
        }

        // 3. Clear search or reset filters in Live TV
        if (_currentTab.value == NavTab.LIVE_TV) {
            if (_searchQuery.value.isNotBlank()) {
                _searchQuery.value = ""
                return true
            }
            if (_selectedLanguage.value != "All" || _selectedContentCategory.value != "All") {
                _selectedLanguage.value = "All"
                _selectedContentCategory.value = "All"
                _selectedCategory.value = "All"
                return true
            }
        }

        // 4. Pop navigation stack
        while (navStack.isNotEmpty()) {
            val prevTab = navStack.removeAt(navStack.size - 1)
            if (prevTab != _currentTab.value) {
                _currentTab.value = prevTab
                return true
            }
        }

        // 5. At Home: If not already showing exit dialog, show it
        if (_currentTab.value == NavTab.HOME) {
            _showExitDialog.value = true
            return true
        }

        // Fallback to Home
        _currentTab.value = NavTab.HOME
        return true
    }

    fun openAddPlaylistDialog() {
        _showAddPlaylistDialog.value = true
    }

    fun dismissAddPlaylistDialog() {
        _showAddPlaylistDialog.value = false
        clearImportStatus()
    }

    fun dismissExitDialog() {
        _showExitDialog.value = false
    }

    fun selectPlaylist(playlistId: Long) {
        _activePlaylistId.value = playlistId
        _selectedCategory.value = "All"
        _selectedLanguage.value = "All"
        _selectedContentCategory.value = "All"
        _searchQuery.value = ""
        viewModelScope.launch {
            preferences.setActivePlaylistId(playlistId)
            playlistRepository.updateLastUsed(playlistId)
        }
    }

    fun selectLanguage(language: String?) {
        _selectedLanguage.value = language ?: "All"
    }

    fun selectContentCategory(category: String?) {
        _selectedContentCategory.value = category ?: "All"
        _selectedCategory.value = category ?: "All"
    }

    fun selectCategory(category: String?) {
        _selectedCategory.value = category
        _selectedContentCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun openChannelActions(channel: Channel) {
        _channelForActions.value = channel
    }

    fun closeChannelActions() {
        _channelForActions.value = null
    }

    fun openLanguageEdit(channel: Channel) {
        _channelForActions.value = null
        _channelForLanguageEdit.value = channel
    }

    fun closeLanguageEdit() {
        _channelForLanguageEdit.value = null
    }

    fun updateChannelLanguage(channelId: Long, newLanguage: String?) {
        viewModelScope.launch {
            channelRepository.updateChannelLanguageOverride(channelId, newLanguage)
            closeLanguageEdit()
        }
    }

    fun showClearHistoryDialog() {
        _showClearHistoryDialog.value = true
    }

    fun dismissClearHistoryDialog() {
        _showClearHistoryDialog.value = false
    }

    fun clearPlaybackHistory() {
        viewModelScope.launch {
            channelRepository.clearRecentHistory()
            _showClearHistoryDialog.value = false
        }
    }

    fun showClearCacheDialog() {
        _showClearCacheDialog.value = true
    }

    fun dismissClearCacheDialog() {
        _showClearCacheDialog.value = false
    }

    fun clearCachedChannels() {
        viewModelScope.launch {
            channelRepository.clearChannelCache()
            _showClearCacheDialog.value = false
            // Re-sync active playlist channels
            refreshActivePlaylist()
        }
    }

    fun importFromUrl(url: String, name: String? = null) {
        viewModelScope.launch {
            _importStatus.value = ImportStatus.Loading("Validating and downloading playlist from URL…")
            val result = playlistRepository.importFromUrl(url, name)
            _importStatus.value = result
            if (result is ImportStatus.Success) {
                selectPlaylist(result.playlist.id)
            }
        }
    }

    fun refreshPlaylist(playlistId: Long) {
        viewModelScope.launch {
            _refreshingPlaylistId.value = playlistId
            _importStatus.value = ImportStatus.Loading("Refreshing playlist channels…")
            val result = playlistRepository.refreshPlaylist(playlistId)
            _importStatus.value = result
            _refreshingPlaylistId.value = null
        }
    }

    fun refreshActivePlaylist() {
        val activeId = _activePlaylistId.value ?: return
        refreshPlaylist(activeId)
    }

    fun renamePlaylist(playlistId: Long, newName: String) {
        viewModelScope.launch {
            playlistRepository.renamePlaylist(playlistId, newName)
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            val result = playlistRepository.deletePlaylist(playlistId)
            if (result.isSuccess) {
                if (_activePlaylistId.value == playlistId) {
                    // Switch to default Tamil TV playlist if active one was deleted
                    val defaultPl = playlistRepository.getDefaultPlaylist()
                    if (defaultPl != null) {
                        selectPlaylist(defaultPl.id)
                    } else {
                        _activePlaylistId.value = null
                    }
                }
            } else {
                _importStatus.value = ImportStatus.Error(
                    result.exceptionOrNull()?.message ?: "Failed to delete playlist"
                )
            }
        }
    }

    fun importFromUri(uri: Uri, name: String? = null) {
        viewModelScope.launch {
            _importStatus.value = ImportStatus.Loading("Reading local M3U file…")
            val result = playlistRepository.importFromUri(uri, name)
            _importStatus.value = result
            if (result is ImportStatus.Success) {
                selectPlaylist(result.playlist.id)
            }
        }
    }

    fun loadDemoChannels() {
        viewModelScope.launch {
            _importStatus.value = ImportStatus.Loading("Loading demo channels…")
            val result = playlistRepository.loadDemoChannels()
            _importStatus.value = result
            if (result is ImportStatus.Success) {
                selectPlaylist(result.playlist.id)
            }
        }
    }

    fun clearImportStatus() {
        _importStatus.value = ImportStatus.Idle
    }

    fun playChannel(channel: Channel) {
        _activePlayingChannelId.value = channel.id
        playerManager.playChannel(channel)
        viewModelScope.launch {
            channelRepository.recordPlayback(channel)
            preferences.setLastPlayedChannelId(channel.id)
        }
    }

    fun playNextChannel() {
        val current = playerUiState.value.currentChannel ?: return
        val currentList = channels.value
        val index = currentList.indexOfFirst { it.id == current.id }
        if (index != -1 && index + 1 < currentList.size) {
            playChannel(currentList[index + 1])
        } else if (currentList.isNotEmpty()) {
            playChannel(currentList.first())
        }
    }

    fun playPreviousChannel() {
        val current = playerUiState.value.currentChannel ?: return
        val currentList = channels.value
        val index = currentList.indexOfFirst { it.id == current.id }
        if (index > 0) {
            playChannel(currentList[index - 1])
        } else if (currentList.isNotEmpty()) {
            playChannel(currentList.last())
        }
    }

    fun toggleFavorite(channel: Channel) {
        viewModelScope.launch {
            channelRepository.toggleFavorite(channel.id, !channel.isFavorite)
        }
    }

    fun playLocalFile(uri: Uri, fileName: String) {
        viewModelScope.launch {
            val localChannel = channelRepository.addLocalMediaChannel(fileName, uri.toString())
            playChannel(localChannel)
        }
    }

    fun closePlayer() {
        playerManager.stop()
    }
}

class MainViewModelFactory(
    private val app: StreamDeckTvApp
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(
            playlistRepository = app.playlistRepository,
            channelRepository = app.channelRepository,
            preferences = app.preferences,
            playerManager = app.playerManager,
            networkMonitor = app.networkMonitor
        ) as T
    }
}
