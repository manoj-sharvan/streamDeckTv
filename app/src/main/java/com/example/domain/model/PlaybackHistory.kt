package com.example.domain.model

data class PlaybackHistory(
    val id: Long = 0,
    val channelId: Long,
    val channelName: String,
    val streamUrl: String,
    val groupTitle: String,
    val tvgLogo: String?,
    val timestamp: Long = System.currentTimeMillis()
)
