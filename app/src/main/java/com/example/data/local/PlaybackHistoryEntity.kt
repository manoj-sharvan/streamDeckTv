package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.domain.model.PlaybackHistory

@Entity(
    tableName = "playback_history",
    indices = [
        Index(value = ["channelId"], unique = true),
        Index(value = ["timestamp"])
    ]
)
data class PlaybackHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val channelId: Long,
    val channelName: String,
    val streamUrl: String,
    val groupTitle: String,
    val tvgLogo: String?,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toDomain(): PlaybackHistory = PlaybackHistory(
        id = id,
        channelId = channelId,
        channelName = channelName,
        streamUrl = streamUrl,
        groupTitle = groupTitle,
        tvgLogo = tvgLogo,
        timestamp = timestamp
    )

    companion object {
        fun fromDomain(history: PlaybackHistory): PlaybackHistoryEntity = PlaybackHistoryEntity(
            id = history.id,
            channelId = history.channelId,
            channelName = history.channelName,
            streamUrl = history.streamUrl,
            groupTitle = history.groupTitle,
            tvgLogo = history.tvgLogo,
            timestamp = history.timestamp
        )
    }
}
