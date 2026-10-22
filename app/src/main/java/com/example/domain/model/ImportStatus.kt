package com.example.domain.model

sealed class ImportStatus {
    object Idle : ImportStatus()
    data class Loading(val message: String) : ImportStatus()
    data class Success(
        val playlist: Playlist,
        val count: Int,
        val isExisting: Boolean = false
    ) : ImportStatus()
    data class Error(val error: String) : ImportStatus()
}
