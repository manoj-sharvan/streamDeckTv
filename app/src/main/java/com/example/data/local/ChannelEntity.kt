package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.domain.model.Channel

@Entity(
    tableName = "channels",
    indices = [
        Index(value = ["playlistId"]),
        Index(value = ["groupTitle"]),
        Index(value = ["language"]),
        Index(value = ["contentCategory"]),
        Index(value = ["isFavorite"])
    ]
)
data class ChannelEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val playlistId: Long,
    val name: String,
    val streamUrl: String,
    val groupTitle: String,
    val tvgId: String?,
    val tvgLogo: String?,
    val isFavorite: Boolean,
    val orderIndex: Int,
    val isLocalMedia: Boolean = false,
    val language: String = "Unknown",
    val userLanguageOverride: String? = null,
    val contentCategory: String = "General"
) {
    fun toDomain(): Channel = Channel(
        id = id,
        playlistId = playlistId,
        name = name,
        streamUrl = streamUrl,
        groupTitle = groupTitle,
        tvgId = tvgId,
        tvgLogo = tvgLogo,
        isFavorite = isFavorite,
        orderIndex = orderIndex,
        isLocalMedia = isLocalMedia,
        language = language,
        userLanguageOverride = userLanguageOverride,
        contentCategory = contentCategory
    )

    companion object {
        fun fromDomain(channel: Channel): ChannelEntity = ChannelEntity(
            id = channel.id,
            playlistId = channel.playlistId,
            name = channel.name,
            streamUrl = channel.streamUrl,
            groupTitle = channel.groupTitle,
            tvgId = channel.tvgId,
            tvgLogo = channel.tvgLogo,
            isFavorite = channel.isFavorite,
            orderIndex = channel.orderIndex,
            isLocalMedia = channel.isLocalMedia,
            language = channel.language,
            userLanguageOverride = channel.userLanguageOverride,
            contentCategory = channel.contentCategory
        )
    }
}
