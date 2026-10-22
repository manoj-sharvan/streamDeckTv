package com.example.data.repository

import com.example.data.local.ChannelDao
import com.example.data.local.ChannelEntity
import com.example.data.local.PlaybackHistoryDao
import com.example.data.local.PlaybackHistoryEntity
import com.example.domain.model.Channel
import com.example.domain.model.PlaybackHistory
import com.example.domain.repository.IChannelRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ChannelRepository(
    private val channelDao: ChannelDao,
    private val playbackHistoryDao: PlaybackHistoryDao
) : IChannelRepository {

    override fun getChannelsByPlaylist(playlistId: Long): Flow<List<Channel>> =
        channelDao.getChannelsByPlaylist(playlistId).map { list -> list.map { it.toDomain() } }

    override fun getCategoriesByPlaylist(playlistId: Long): Flow<List<String>> =
        channelDao.getCategoriesByPlaylist(playlistId)

    override fun getLanguagesByPlaylist(playlistId: Long): Flow<List<String>> =
        channelDao.getLanguagesByPlaylist(playlistId).map { list ->
            list.filter { it.isNotBlank() }
        }

    override fun getContentCategoriesByPlaylist(playlistId: Long): Flow<List<String>> =
        channelDao.getContentCategoriesByPlaylist(playlistId).map { list ->
            list.filter { it.isNotBlank() }
        }

    override fun getAllFavorites(): Flow<List<Channel>> =
        channelDao.getAllFavoriteChannels().map { list -> list.map { it.toDomain() } }

    override fun getFavoritesByPlaylist(playlistId: Long): Flow<List<Channel>> =
        channelDao.getFavoritesByPlaylist(playlistId).map { list -> list.map { it.toDomain() } }

    override fun searchChannels(playlistId: Long, query: String): Flow<List<Channel>> =
        channelDao.searchChannels(playlistId, query).map { list -> list.map { it.toDomain() } }

    override val recentChannels: Flow<List<PlaybackHistory>> =
        playbackHistoryDao.getRecentChannels().map { list -> list.take(5).map { it.toDomain() } }

    override suspend fun getChannelById(id: Long): Channel? = withContext(Dispatchers.IO) {
        channelDao.getChannelById(id)?.toDomain()
    }

    override suspend fun toggleFavorite(channelId: Long, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        channelDao.setFavorite(channelId, isFavorite)
    }

    override suspend fun updateChannelLanguageOverride(channelId: Long, language: String?) = withContext(Dispatchers.IO) {
        channelDao.updateChannelLanguageOverride(channelId, language)
    }

    override suspend fun recordPlayback(channel: Channel) = withContext(Dispatchers.IO) {
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

    override suspend fun clearRecentHistory() = withContext(Dispatchers.IO) {
        playbackHistoryDao.clearHistory()
    }

    override suspend fun clearChannelCache() = withContext(Dispatchers.IO) {
        channelDao.deleteAllCachedChannels()
    }

    override suspend fun addLocalMediaChannel(name: String, uri: String): Channel = withContext(Dispatchers.IO) {
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
        val generatedId = channelDao.insertChannel(entity)
        entity.copy(id = generatedId).toDomain()
    }
}
