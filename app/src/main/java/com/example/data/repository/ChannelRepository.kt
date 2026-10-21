package com.example.data.repository

import com.example.data.local.ChannelDao
import com.example.data.local.ChannelEntity
import com.example.data.local.PlaybackHistoryDao
import com.example.data.local.PlaybackHistoryEntity
import com.example.domain.model.Channel
import com.example.domain.model.PlaybackHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ChannelRepository(
    private val channelDao: ChannelDao,
    private val playbackHistoryDao: PlaybackHistoryDao
) {

    fun getChannelsByPlaylist(playlistId: Long): Flow<List<Channel>> =
        channelDao.getChannelsByPlaylist(playlistId).map { list -> list.map { it.toDomain() } }

    fun getCategoriesByPlaylist(playlistId: Long): Flow<List<String>> =
        channelDao.getCategoriesByPlaylist(playlistId)

    fun getLanguagesByPlaylist(playlistId: Long): Flow<List<String>> =
        channelDao.getLanguagesByPlaylist(playlistId).map { list ->
            list.filter { it.isNotBlank() }
        }

    fun getContentCategoriesByPlaylist(playlistId: Long): Flow<List<String>> =
        channelDao.getContentCategoriesByPlaylist(playlistId).map { list ->
            list.filter { it.isNotBlank() }
        }

    fun getAllFavorites(): Flow<List<Channel>> =
        channelDao.getAllFavoriteChannels().map { list -> list.map { it.toDomain() } }

    fun getFavoritesByPlaylist(playlistId: Long): Flow<List<Channel>> =
        channelDao.getFavoritesByPlaylist(playlistId).map { list -> list.map { it.toDomain() } }

    fun searchChannels(playlistId: Long, query: String): Flow<List<Channel>> =
        channelDao.searchChannels(playlistId, query).map { list -> list.map { it.toDomain() } }

    val recentChannels: Flow<List<PlaybackHistory>> =
        playbackHistoryDao.getRecentChannels().map { list -> list.take(5).map { it.toDomain() } }

    suspend fun getChannelById(id: Long): Channel? = withContext(Dispatchers.IO) {
        channelDao.getChannelById(id)?.toDomain()
    }

    suspend fun toggleFavorite(channelId: Long, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        channelDao.setFavorite(channelId, isFavorite)
    }

    suspend fun updateChannelLanguageOverride(channelId: Long, language: String?) = withContext(Dispatchers.IO) {
        channelDao.updateChannelLanguageOverride(channelId, language)
    }

    suspend fun recordPlayback(channel: Channel) = withContext(Dispatchers.IO) {
        playbackHistoryDao.deleteByChannelOrUrl(channel.id, channel.streamUrl)
        playbackHistoryDao.recordHistory(
            PlaybackHistoryEntity(
                channelId = channel.id,
                channelName = channel.name,
                streamUrl = channel.streamUrl,
                groupTitle = channel.groupTitle,
                tvgLogo = channel.tvgLogo,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun clearRecentHistory() = withContext(Dispatchers.IO) {
        playbackHistoryDao.clearHistory()
    }

    suspend fun clearChannelCache() = withContext(Dispatchers.IO) {
        channelDao.deleteAllCachedChannels()
    }

    suspend fun addLocalMediaChannel(name: String, uri: String): Channel = withContext(Dispatchers.IO) {
        val entity = ChannelEntity(
            playlistId = -1L,
            name = name,
            streamUrl = uri,
            groupTitle = "Local Media",
            tvgId = null,
            tvgLogo = null,
            isFavorite = false,
            orderIndex = 0,
            isLocalMedia = true,
            language = "Local",
            userLanguageOverride = null,
            contentCategory = "Movies"
        )
        Channel(
            id = System.currentTimeMillis(),
            playlistId = -1L,
            name = name,
            streamUrl = uri,
            groupTitle = "Local Media",
            isLocalMedia = true,
            language = "Local",
            contentCategory = "Movies"
        )
    }
}
