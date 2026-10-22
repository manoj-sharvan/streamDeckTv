package com.example.domain.usecase

import com.example.domain.model.Channel
import com.example.domain.repository.IChannelRepository
import com.example.player.StreamPlayerManager

/**
 * Use case to coordinate channel playback through StreamPlayerManager and log playback history.
 */
class PlayChannelUseCase(
    private val channelRepository: IChannelRepository,
    private val playerManager: StreamPlayerManager
) {
    suspend operator fun invoke(channel: Channel) {
        playerManager.playChannel(channel)
        channelRepository.recordPlayback(channel)
    }
}
