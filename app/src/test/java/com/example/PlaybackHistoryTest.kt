package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.PlaybackHistoryDao
import com.example.data.local.PlaybackHistoryEntity
import com.example.data.local.StreamDeckDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlaybackHistoryTest {

    private lateinit var database: StreamDeckDatabase
    private lateinit var historyDao: PlaybackHistoryDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, StreamDeckDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        historyDao = database.playbackHistoryDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testRecentChannelsLimitsToLast5() = runBlocking {
        // Record 7 channels with increasing timestamps
        for (i in 1..7) {
            historyDao.recordHistory(
                PlaybackHistoryEntity(
                    channelId = i.toLong(),
                    channelName = "Channel $i",
                    streamUrl = "https://example.com/stream$i.m3u8",
                    groupTitle = "Group",
                    tvgLogo = null,
                    timestamp = i * 1000L
                )
            )
        }

        val recents = historyDao.getRecentChannels().first()

        // Verify exactly 5 channels are returned
        assertEquals(5, recents.size)

        // Verify they are the most recent 5 in descending order (7, 6, 5, 4, 3)
        assertEquals("Channel 7", recents[0].channelName)
        assertEquals("Channel 6", recents[1].channelName)
        assertEquals("Channel 5", recents[2].channelName)
        assertEquals("Channel 4", recents[3].channelName)
        assertEquals("Channel 3", recents[4].channelName)
    }

    @Test
    fun testReplayingChannelBringsItToFront() = runBlocking {
        for (i in 1..5) {
            historyDao.recordHistory(
                PlaybackHistoryEntity(
                    channelId = i.toLong(),
                    channelName = "Channel $i",
                    streamUrl = "https://example.com/stream$i.m3u8",
                    groupTitle = "Group",
                    tvgLogo = null,
                    timestamp = i * 1000L
                )
            )
        }

        // Replay Channel 2 at a later timestamp
        historyDao.deleteByChannelOrUrl(2L, "https://example.com/stream2.m3u8")
        historyDao.recordHistory(
            PlaybackHistoryEntity(
                channelId = 2L,
                channelName = "Channel 2",
                streamUrl = "https://example.com/stream2.m3u8",
                groupTitle = "Group",
                tvgLogo = null,
                timestamp = 10000L
            )
        )

        val recents = historyDao.getRecentChannels().first()
        assertEquals(5, recents.size)
        // Channel 2 should now be at the very front
        assertEquals("Channel 2", recents[0].channelName)
    }
}
