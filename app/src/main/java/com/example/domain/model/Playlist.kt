package com.example.domain.model

data class Playlist(
    val id: Long = 0,
    val name: String,
    val sourceUri: String,
    val isLocal: Boolean,
    val isDefault: Boolean = false,
    val channelCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsed: Long = System.currentTimeMillis(),
    val lastRefreshed: Long = System.currentTimeMillis()
)
