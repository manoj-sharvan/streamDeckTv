package com.example.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class AudioTrackInfo(
    val id: String,
    val index: Int,
    val label: String,
    val language: String?,
    val isSelected: Boolean
)

@Immutable
data class SubtitleTrackInfo(
    val id: String,
    val index: Int,
    val label: String,
    val language: String?,
    val isSelected: Boolean
)
