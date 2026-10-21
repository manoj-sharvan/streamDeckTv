package com.example.domain.model

data class AudioTrackInfo(
    val id: String,
    val index: Int,
    val label: String,
    val language: String?,
    val isSelected: Boolean
)

data class SubtitleTrackInfo(
    val id: String,
    val index: Int,
    val label: String,
    val language: String?,
    val isSelected: Boolean
)
