package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.Playlist

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val sourceUri: String,
    val isLocal: Boolean,
    val isDefault: Boolean = false,
    val channelCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsed: Long = System.currentTimeMillis(),
    val lastRefreshed: Long = System.currentTimeMillis()
) {
    fun toDomain(): Playlist = Playlist(
        id = id,
        name = name,
        sourceUri = sourceUri,
        isLocal = isLocal,
        isDefault = isDefault,
        channelCount = channelCount,
        createdAt = createdAt,
        lastUsed = lastUsed,
        lastRefreshed = lastRefreshed
    )

    companion object {
        fun fromDomain(playlist: Playlist): PlaylistEntity = PlaylistEntity(
            id = playlist.id,
            name = playlist.name,
            sourceUri = playlist.sourceUri,
            isLocal = playlist.isLocal,
            isDefault = playlist.isDefault,
            channelCount = playlist.channelCount,
            createdAt = playlist.createdAt,
            lastUsed = playlist.lastUsed,
            lastRefreshed = playlist.lastRefreshed
        )
    }
}
