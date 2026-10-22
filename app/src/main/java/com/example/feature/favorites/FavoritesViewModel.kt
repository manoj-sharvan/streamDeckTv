package com.example.feature.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppPreferences
import com.example.domain.model.Channel
import com.example.domain.repository.IChannelRepository
import com.example.domain.usecase.PlayChannelUseCase
import com.example.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Scoped ViewModel for FavoritesScreen managing saved channel list and interactions.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModel(
    private val channelRepository: IChannelRepository,
    private val preferences: AppPreferences,
    private val playChannelUseCase: PlayChannelUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val _activePlaylistId = MutableStateFlow<Long?>(null)
    val activePlaylistId: StateFlow<Long?> = _activePlaylistId.asStateFlow()

    val favorites: StateFlow<List<Channel>> = _activePlaylistId.flatMapLatest { id ->
        if (id == null) {
            channelRepository.getAllFavorites()
        } else {
            channelRepository.getFavoritesByPlaylist(id)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            preferences.activePlaylistId.collect { id ->
                _activePlaylistId.value = id
            }
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
}
