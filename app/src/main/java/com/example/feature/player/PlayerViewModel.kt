package com.example.feature.player

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppPreferences
import com.example.domain.model.AudioTrackInfo
import com.example.domain.model.Channel
import com.example.domain.model.SubtitleTrackInfo
import com.example.domain.repository.IChannelRepository
import com.example.domain.usecase.PlayChannelUseCase
import com.example.player.PlayerUiState
import com.example.player.StreamPlayerManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Scoped ViewModel for PlayerScreen handling stream playback, audio/subtitle tracks, and channel hopping.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModel(
    val playerManager: StreamPlayerManager,
    private val channelRepository: IChannelRepository,
    private val preferences: AppPreferences,
    private val playChannelUseCase: PlayChannelUseCase
) : ViewModel() {

    val uiState: StateFlow<PlayerUiState> = playerManager.uiState

    private val _activePlayingChannel = MutableStateFlow<Channel?>(null)
    val activePlayingChannel: StateFlow<Channel?> = _activePlayingChannel.asStateFlow()

    private val _activePlaylistId = MutableStateFlow<Long?>(null)

    val currentPlaylistChannels: StateFlow<List<Channel>> = _activePlaylistId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else channelRepository.getChannelsByPlaylist(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            preferences.activePlaylistId.collect { id ->
                _activePlaylistId.value = id
            }
        }
    }

    fun playChannel(channel: Channel) {
        _activePlayingChannel.value = channel
        viewModelScope.launch {
            playChannelUseCase(channel)
        }
    }

    fun playLocalFile(uri: Uri, fileName: String) {
        viewModelScope.launch {
            val localChannel = channelRepository.addLocalMediaChannel(fileName, uri.toString())
            playChannel(localChannel)
        }
    }

    fun nextChannel() {
        val current = _activePlayingChannel.value ?: return
        val list = currentPlaylistChannels.value
        if (list.isEmpty()) return
        val index = list.indexOfFirst { it.id == current.id }
        if (index != -1 && index < list.size - 1) {
            playChannel(list[index + 1])
        } else {
            playChannel(list.first())
        }
    }

    fun previousChannel() {
        val current = _activePlayingChannel.value ?: return
        val list = currentPlaylistChannels.value
        if (list.isEmpty()) return
        val index = list.indexOfFirst { it.id == current.id }
        if (index > 0) {
            playChannel(list[index - 1])
        } else {
            playChannel(list.last())
        }
    }

    fun stop() {
        playerManager.stop()
        _activePlayingChannel.value = null
    }

    fun selectAudioTrack(trackInfo: AudioTrackInfo) {
        playerManager.selectAudioTrack(trackInfo)
    }

    fun selectSubtitleTrack(trackInfo: SubtitleTrackInfo?) {
        playerManager.selectSubtitleTrack(trackInfo)
    }
}
