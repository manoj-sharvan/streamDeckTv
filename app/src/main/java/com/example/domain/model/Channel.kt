package com.example.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Channel(
    val id: Long = 0,
    val playlistId: Long,
    val name: String,
    val streamUrl: String,
    val groupTitle: String = "General",
    val tvgId: String? = null,
    val tvgLogo: String? = null,
    val isFavorite: Boolean = false,
    val orderIndex: Int = 0,
    val isLocalMedia: Boolean = false,
    val language: String = "Unknown",
    val userLanguageOverride: String? = null,
    val contentCategory: String = "General"
) {
    val effectiveLanguage: String
        get() = userLanguageOverride?.takeIf { it.isNotBlank() } ?: language

    val isLanguageVerified: Boolean
        get() = userLanguageOverride != null || (language != "Unknown" && language != "Other")
}
