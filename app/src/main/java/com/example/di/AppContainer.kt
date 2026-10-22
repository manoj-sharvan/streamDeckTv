package com.example.di

import android.content.Context
import com.example.data.local.AppPreferences
import com.example.data.local.StreamDeckDatabase
import com.example.data.repository.ChannelRepository
import com.example.data.repository.PlaylistRepository
import com.example.domain.repository.IChannelRepository
import com.example.domain.repository.IPlaylistRepository
import com.example.domain.usecase.GetChannelsUseCase
import com.example.domain.usecase.ImportPlaylistUseCase
import com.example.domain.usecase.ManagePlaylistUseCase
import com.example.domain.usecase.PlayChannelUseCase
import com.example.domain.usecase.ToggleFavoriteUseCase
import com.example.player.StreamPlayerManager
import com.example.util.NetworkMonitor

/**
 * Dependency Injection container interface declaring application-wide singletons and use cases.
 */
interface AppContainer {
    val database: StreamDeckDatabase
    val preferences: AppPreferences
    val networkMonitor: NetworkMonitor
    val playlistRepository: IPlaylistRepository
    val channelRepository: IChannelRepository
    val playerManager: StreamPlayerManager

    val getChannelsUseCase: GetChannelsUseCase
    val importPlaylistUseCase: ImportPlaylistUseCase
    val toggleFavoriteUseCase: ToggleFavoriteUseCase
    val playChannelUseCase: PlayChannelUseCase
    val managePlaylistUseCase: ManagePlaylistUseCase
}

/**
 * Production implementation of [AppContainer] using lazy initialization.
 */
class DefaultAppContainer(private val context: Context) : AppContainer {
    override val database: StreamDeckDatabase by lazy {
        StreamDeckDatabase.getInstance(context)
    }

    override val preferences: AppPreferences by lazy {
        AppPreferences(context)
    }

    override val networkMonitor: NetworkMonitor by lazy {
        NetworkMonitor(context)
    }

    override val playlistRepository: IPlaylistRepository by lazy {
        PlaylistRepository(
            context = context,
            playlistDao = database.playlistDao(),
            channelDao = database.channelDao()
        )
    }

    override val channelRepository: IChannelRepository by lazy {
        ChannelRepository(
            channelDao = database.channelDao(),
            playbackHistoryDao = database.playbackHistoryDao()
        )
    }

    override val playerManager: StreamPlayerManager by lazy {
        StreamPlayerManager(context)
    }

    override val getChannelsUseCase: GetChannelsUseCase by lazy {
        GetChannelsUseCase(channelRepository)
    }

    override val importPlaylistUseCase: ImportPlaylistUseCase by lazy {
        ImportPlaylistUseCase(playlistRepository)
    }

    override val toggleFavoriteUseCase: ToggleFavoriteUseCase by lazy {
        ToggleFavoriteUseCase(channelRepository)
    }

    override val playChannelUseCase: PlayChannelUseCase by lazy {
        PlayChannelUseCase(channelRepository, playerManager)
    }

    override val managePlaylistUseCase: ManagePlaylistUseCase by lazy {
        ManagePlaylistUseCase(playlistRepository)
    }
}
