package com.example.feature.live

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppPreferences
import com.example.data.parser.LanguageClassifier
import com.example.domain.model.Channel
import com.example.domain.repository.IChannelRepository
import com.example.domain.usecase.GetChannelsUseCase
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
 * Scoped ViewModel for LiveTvScreen managing channels, language & category chips, search queries, and playback.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LiveTvViewModel(
    private val channelRepository: IChannelRepository,
    private val preferences: AppPreferences,
    private val getChannelsUseCase: GetChannelsUseCase,
    private val playChannelUseCase: PlayChannelUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    networkMonitor: NetworkMonitor
) : ViewModel() {

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline

    private val _activePlaylistId = MutableStateFlow<Long?>(null)
    val activePlaylistId: StateFlow<Long?> = _activePlaylistId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedLanguage = MutableStateFlow<String?>("All")
    val selectedLanguage: StateFlow<String?> = _selectedLanguage.asStateFlow()

    private val _selectedContentCategory = MutableStateFlow<String?>("All")
    val selectedContentCategory: StateFlow<String?> = _selectedContentCategory.asStateFlow()

    val channels: StateFlow<List<Channel>> = _activePlaylistId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else channelRepository.getChannelsByPlaylist(id)
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

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onLanguageSelect(language: String?) {
        _selectedLanguage.value = language
    }

    fun onContentCategorySelect(category: String?) {
        _selectedContentCategory.value = category
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

    fun updateLanguageOverride(channelId: Long, language: String?) {
        viewModelScope.launch {
            channelRepository.updateChannelLanguageOverride(channelId, language)
        }
    }
}
