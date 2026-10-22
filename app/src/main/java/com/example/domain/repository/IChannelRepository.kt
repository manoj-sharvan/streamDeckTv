package com.example.domain.repository

import com.example.domain.model.Channel
import com.example.domain.model.PlaybackHistory
import kotlinx.coroutines.flow.Flow

/**
 * Interface representing channel data operations.
 */
interface IChannelRepository {
    fun getChannelsByPlaylist(playlistId: Long): Flow<List<Channel>>
    fun getCategoriesByPlaylist(playlistId: Long): Flow<List<String>>
    fun getLanguagesByPlaylist(playlistId: Long): Flow<List<String>>
    fun getContentCategoriesByPlaylist(playlistId: Long): Flow<List<String>>
    fun getAllFavorites(): Flow<List<Channel>>
    fun getFavoritesByPlaylist(playlistId: Long): Flow<List<Channel>>
    fun searchChannels(playlistId: Long, query: String): Flow<List<Channel>>
    val recentChannels: Flow<List<PlaybackHistory>>
    suspend fun getChannelById(id: Long): Channel?
    suspend fun toggleFavorite(channelId: Long, isFavorite: Boolean)
    suspend fun updateChannelLanguageOverride(channelId: Long, language: String?)
    suspend fun recordPlayback(channel: Channel)
    suspend fun clearRecentHistory()
    suspend fun clearChannelCache()
    suspend fun addLocalMediaChannel(name: String, uri: String): Channel
}
