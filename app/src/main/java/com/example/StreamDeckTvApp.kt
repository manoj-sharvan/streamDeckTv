package com.example

import android.app.Application
import com.example.data.local.AppPreferences
import com.example.data.local.StreamDeckDatabase
import com.example.di.AppContainer
import com.example.di.DefaultAppContainer
import com.example.domain.repository.IChannelRepository
import com.example.domain.repository.IPlaylistRepository
import com.example.player.StreamPlayerManager
import com.example.util.NetworkMonitor

class StreamDeckTvApp : Application() {

    lateinit var appContainer: AppContainer
        private set

    val database: StreamDeckDatabase
        get() = appContainer.database

    val preferences: AppPreferences
        get() = appContainer.preferences

    val playlistRepository: IPlaylistRepository
        get() = appContainer.playlistRepository

    val channelRepository: IChannelRepository
        get() = appContainer.channelRepository

    val playerManager: StreamPlayerManager
        get() = appContainer.playerManager

    val networkMonitor: NetworkMonitor
        get() = appContainer.networkMonitor

    override fun onCreate() {
        super.onCreate()
        instance = this
        appContainer = DefaultAppContainer(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        appContainer.playerManager.release()
    }

    companion object {
        lateinit var instance: StreamDeckTvApp
            private set
    }
}
