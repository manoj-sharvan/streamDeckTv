package com.example.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.MainViewModel
import com.example.feature.favorites.FavoritesViewModel
import com.example.feature.home.HomeViewModel
import com.example.feature.live.LiveTvViewModel
import com.example.feature.player.PlayerViewModel
import com.example.feature.playlists.PlaylistsViewModel
import com.example.feature.settings.SettingsViewModel
import com.example.navigation.NavigationViewModel

/**
 * Universal ViewModelProvider.Factory powered by AppContainer.
 */
class AppViewModelFactory(
    private val container: AppContainer
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(
                    playlistRepository = container.playlistRepository,
                    channelRepository = container.channelRepository,
                    preferences = container.preferences,
                    playChannelUseCase = container.playChannelUseCase,
                    toggleFavoriteUseCase = container.toggleFavoriteUseCase,
                    managePlaylistUseCase = container.managePlaylistUseCase,
                    networkMonitor = container.networkMonitor
                ) as T
            }
            modelClass.isAssignableFrom(LiveTvViewModel::class.java) -> {
                LiveTvViewModel(
                    channelRepository = container.channelRepository,
                    preferences = container.preferences,
                    getChannelsUseCase = container.getChannelsUseCase,
                    playChannelUseCase = container.playChannelUseCase,
                    toggleFavoriteUseCase = container.toggleFavoriteUseCase,
                    networkMonitor = container.networkMonitor
                ) as T
            }
            modelClass.isAssignableFrom(FavoritesViewModel::class.java) -> {
                FavoritesViewModel(
                    channelRepository = container.channelRepository,
                    preferences = container.preferences,
                    playChannelUseCase = container.playChannelUseCase,
                    toggleFavoriteUseCase = container.toggleFavoriteUseCase
                ) as T
            }
            modelClass.isAssignableFrom(PlaylistsViewModel::class.java) -> {
                PlaylistsViewModel(
                    managePlaylistUseCase = container.managePlaylistUseCase,
                    importPlaylistUseCase = container.importPlaylistUseCase,
                    preferences = container.preferences
                ) as T
            }
            modelClass.isAssignableFrom(PlayerViewModel::class.java) -> {
                PlayerViewModel(
                    playerManager = container.playerManager,
                    channelRepository = container.channelRepository,
                    preferences = container.preferences,
                    playChannelUseCase = container.playChannelUseCase
                ) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(
                    channelRepository = container.channelRepository,
                    managePlaylistUseCase = container.managePlaylistUseCase,
                    preferences = container.preferences
                ) as T
            }
            modelClass.isAssignableFrom(NavigationViewModel::class.java) -> {
                NavigationViewModel() as T
            }
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(
                    playlistRepository = container.playlistRepository,
                    channelRepository = container.channelRepository,
                    preferences = container.preferences,
                    playerManager = container.playerManager,
                    networkMonitor = container.networkMonitor
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
