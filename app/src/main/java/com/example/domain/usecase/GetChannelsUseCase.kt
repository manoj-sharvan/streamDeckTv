package com.example.domain.usecase

import com.example.domain.model.Channel
import com.example.domain.repository.IChannelRepository
import com.example.util.SearchFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Use case to retrieve channels for a playlist, with optional search, language, and category filtering.
 */
class GetChannelsUseCase(
    private val channelRepository: IChannelRepository
) {
    operator fun invoke(
        playlistId: Long,
        query: String = "",
        language: String? = null,
        category: String? = null
    ): Flow<List<Channel>> {
        return channelRepository.getChannelsByPlaylist(playlistId).map { channels ->
            if (query.isBlank() &&
                (language == null || language.equals("All", ignoreCase = true)) &&
                (category == null || category.equals("All", ignoreCase = true))
            ) {
                channels
            } else {
                SearchFilter.filter(
                    channels = channels,
                    query = query,
                    selectedLanguage = language,
                    selectedCategory = category
                )
            }
        }
    }
}
