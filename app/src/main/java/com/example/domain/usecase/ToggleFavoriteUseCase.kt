package com.example.domain.usecase

import com.example.domain.model.Channel
import com.example.domain.repository.IChannelRepository

/**
 * Handles toggling favorite state for a channel.
 */
class ToggleFavoriteUseCase(
    private val channelRepository: IChannelRepository
) {
    suspend operator fun invoke(channel: Channel) {
        channelRepository.toggleFavorite(channel.id, !channel.isFavorite)
    }

    suspend operator fun invoke(channelId: Long, isFavorite: Boolean) {
        channelRepository.toggleFavorite(channelId, isFavorite)
    }
}
