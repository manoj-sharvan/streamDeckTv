package com.example

import android.app.Application
import com.example.data.local.AppPreferences
import com.example.data.local.StreamDeckDatabase
import com.example.data.repository.ChannelRepository
import com.example.data.repository.PlaylistRepository
import com.example.player.StreamPlayerManager

class StreamDeckTvApp : Application() {

    lateinit var database: StreamDeckDatabase
        private set

    lateinit var preferences: AppPreferences
        private set

    lateinit var playlistRepository: PlaylistRepository
        private set

    lateinit var channelRepository: ChannelRepository
        private set

    lateinit var playerManager: StreamPlayerManager
        private set

    lateinit var networkMonitor: com.example.util.NetworkMonitor
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = StreamDeckDatabase.getInstance(this)
        preferences = AppPreferences(this)
        networkMonitor = com.example.util.NetworkMonitor(this)
        playlistRepository = PlaylistRepository(
            context = this,
            playlistDao = database.playlistDao(),
            channelDao = database.channelDao()
        )
        channelRepository = ChannelRepository(
            channelDao = database.channelDao(),
            playbackHistoryDao = database.playbackHistoryDao()
        )
        playerManager = StreamPlayerManager(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        playerManager.release()
    }

    companion object {
        lateinit var instance: StreamDeckTvApp
            private set
    }
}
