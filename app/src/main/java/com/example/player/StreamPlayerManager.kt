package com.example.player

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.example.domain.model.AudioTrackInfo
import com.example.domain.model.Channel
import com.example.domain.model.SubtitleTrackInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PlayerUiState(
    val currentChannel: Channel? = null,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val isLiveStream: Boolean = true,
    val isSeekable: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val errorMessage: String? = null,
    val audioTracks: List<AudioTrackInfo> = emptyList(),
    val subtitleTracks: List<SubtitleTrackInfo> = emptyList(),
    val selectedAudioTrackId: String? = null,
    val selectedSubtitleTrackId: String? = null
)

@OptIn(UnstableApi::class)
class StreamPlayerManager(private val context: Context) {

    private var exoPlayer: ExoPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var progressJob: Job? = null

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_BUFFERING -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = true,
                        errorMessage = null
                    )
                }
                Player.STATE_READY -> {
                    val player = exoPlayer ?: return
                    val isLive = player.isCurrentMediaItemLive
                    val isSeekable = player.isCurrentMediaItemSeekable
                    val duration = if (isLive || !isSeekable) 0L else player.duration.coerceAtLeast(0L)

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isPlaying = player.isPlaying,
                        isLiveStream = isLive,
                        isSeekable = isSeekable && !isLive,
                        durationMs = duration,
                        errorMessage = null
                    )
                    startProgressTracker()
                }
                Player.STATE_ENDED -> {
                    _uiState.value = _uiState.value.copy(
                        isPlaying = false,
                        isLoading = false
                    )
                    stopProgressTracker()
                }
                Player.STATE_IDLE -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false
                    )
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _uiState.value = _uiState.value.copy(isPlaying = isPlaying)
        }

        override fun onPlayerError(error: PlaybackException) {
            val userFriendlyMessage = when (error.errorCode) {
                PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
                PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT ->
                    "Network error connecting to stream. Check internet connection."
                PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS ->
                    "Server error loading stream. The channel may be offline or restricted."
                PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED,
                PlaybackException.ERROR_CODE_PARSING_MANIFEST_MALFORMED ->
                    "Unsupported stream format or corrupted playlist stream."
                PlaybackException.ERROR_CODE_DECODER_INIT_FAILED ->
                    "Hardware video decoder error for this stream."
                else ->
                    error.localizedMessage ?: "Playback error encountered (${error.errorCodeName})"
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isPlaying = false,
                errorMessage = userFriendlyMessage
            )
            stopProgressTracker()
        }

        override fun onTracksChanged(tracks: Tracks) {
            updateAvailableTracks(tracks)
        }
    }

    fun getPlayer(): ExoPlayer {
        return exoPlayer ?: createPlayer().also { exoPlayer = it }
    }

    private fun createPlayer(): ExoPlayer {
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                /* minBufferMs = */ 4_000,
                /* maxBufferMs = */ 15_000,
                /* bufferForPlaybackMs = */ 1_500,
                /* bufferForPlaybackAfterRebufferMs = */ 2_500
            )
            .build()

        val player = ExoPlayer.Builder(context)
            .setLoadControl(loadControl)
            .build()

        player.addListener(playerListener)
        player.playWhenReady = true
        return player
    }

    fun playChannel(channel: Channel) {
        val player = getPlayer()

        _uiState.value = PlayerUiState(
            currentChannel = channel,
            isLoading = true,
            isPlaying = false,
            errorMessage = null
        )

        val uri = Uri.parse(channel.streamUrl)
        val mediaSource = createMediaSource(uri)

        player.stop()
        player.clearMediaItems()
        player.setMediaSource(mediaSource)
        player.prepare()
        player.playWhenReady = true
    }

    private fun createMediaSource(uri: Uri): MediaSource {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("StreamDeck-TV/1.0 (Android TV Media3)")
            .setConnectTimeoutMs(12_000)
            .setReadTimeoutMs(15_000)
            .setAllowCrossProtocolRedirects(true)

        val dataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)

        val urlString = uri.toString().lowercase()
        return if (urlString.contains(".m3u8")) {
            HlsMediaSource.Factory(dataSourceFactory)
                .setAllowChunklessPreparation(true)
                .createMediaSource(MediaItem.fromUri(uri))
        } else {
            DefaultMediaSourceFactory(context)
                .setDataSourceFactory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(uri))
        }
    }

    fun togglePlayPause() {
        val player = exoPlayer ?: return
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    fun retry() {
        val currentChannel = _uiState.value.currentChannel
        if (currentChannel != null) {
            playChannel(currentChannel)
        }
    }

    fun seekTo(positionMs: Long) {
        val player = exoPlayer ?: return
        if (_uiState.value.isSeekable) {
            player.seekTo(positionMs.coerceIn(0L, _uiState.value.durationMs))
        }
    }

    fun seekRelative(offsetMs: Long) {
        val player = exoPlayer ?: return
        if (_uiState.value.isSeekable) {
            val target = (player.currentPosition + offsetMs).coerceIn(0L, _uiState.value.durationMs)
            player.seekTo(target)
        }
    }

    private fun updateAvailableTracks(tracks: Tracks) {
        val audioTracks = mutableListOf<AudioTrackInfo>()
        val subtitleTracks = mutableListOf<SubtitleTrackInfo>()

        for (group in tracks.groups) {
            val trackType = group.type
            for (i in 0 until group.length) {
                val format = group.getTrackFormat(i)
                val isSelected = group.isTrackSelected(i)
                val trackId = "${group.mediaTrackGroup.id}_$i"
                val label = format.label ?: format.language ?: "Track ${i + 1}"

                if (trackType == C.TRACK_TYPE_AUDIO) {
                    audioTracks.add(
                        AudioTrackInfo(
                            id = trackId,
                            index = i,
                            label = label,
                            language = format.language,
                            isSelected = isSelected
                        )
                    )
                } else if (trackType == C.TRACK_TYPE_TEXT) {
                    subtitleTracks.add(
                        SubtitleTrackInfo(
                            id = trackId,
                            index = i,
                            label = label,
                            language = format.language,
                            isSelected = isSelected
                        )
                    )
                }
            }
        }

        _uiState.value = _uiState.value.copy(
            audioTracks = audioTracks,
            subtitleTracks = subtitleTracks,
            selectedAudioTrackId = audioTracks.firstOrNull { it.isSelected }?.id,
            selectedSubtitleTrackId = subtitleTracks.firstOrNull { it.isSelected }?.id
        )
    }

    fun selectAudioTrack(trackInfo: AudioTrackInfo) {
        val player = exoPlayer ?: return
        val currentTracks = player.currentTracks

        for (group in currentTracks.groups) {
            if (group.type == C.TRACK_TYPE_AUDIO) {
                for (i in 0 until group.length) {
                    val id = "${group.mediaTrackGroup.id}_$i"
                    if (id == trackInfo.id) {
                        player.trackSelectionParameters = player.trackSelectionParameters
                            .buildUpon()
                            .setOverrideForType(
                                TrackSelectionOverride(group.mediaTrackGroup, listOf(i))
                            )
                            .build()
                        _uiState.value = _uiState.value.copy(selectedAudioTrackId = trackInfo.id)
                        return
                    }
                }
            }
        }
    }

    fun selectSubtitleTrack(trackInfo: SubtitleTrackInfo?) {
        val player = exoPlayer ?: return
        if (trackInfo == null) {
            // Turn off subtitles
            player.trackSelectionParameters = player.trackSelectionParameters
                .buildUpon()
                .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                .build()
            _uiState.value = _uiState.value.copy(selectedSubtitleTrackId = null)
        } else {
            val currentTracks = player.currentTracks
            for (group in currentTracks.groups) {
                if (group.type == C.TRACK_TYPE_TEXT) {
                    for (i in 0 until group.length) {
                        val id = "${group.mediaTrackGroup.id}_$i"
                        if (id == trackInfo.id) {
                            player.trackSelectionParameters = player.trackSelectionParameters
                                .buildUpon()
                                .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                                .setOverrideForType(
                                    TrackSelectionOverride(group.mediaTrackGroup, listOf(i))
                                )
                                .build()
                            _uiState.value = _uiState.value.copy(selectedSubtitleTrackId = trackInfo.id)
                            return
                        }
                    }
                }
            }
        }
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                val player = exoPlayer
                if (player != null && player.isPlaying && _uiState.value.isSeekable) {
                    _uiState.value = _uiState.value.copy(
                        currentPositionMs = player.currentPosition.coerceAtLeast(0L),
                        durationMs = player.duration.coerceAtLeast(0L)
                    )
                }
                delay(1000)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    fun stop() {
        stopProgressTracker()
        exoPlayer?.stop()
        exoPlayer?.clearMediaItems()
        _uiState.value = PlayerUiState(
            currentChannel = null,
            isPlaying = false,
            isLoading = false,
            errorMessage = null
        )
    }

    fun release() {
        stopProgressTracker()
        exoPlayer?.removeListener(playerListener)
        exoPlayer?.release()
        exoPlayer = null
    }
}
