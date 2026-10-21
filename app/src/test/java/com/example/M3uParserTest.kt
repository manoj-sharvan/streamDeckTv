package com.example

import com.example.data.parser.M3uParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class M3uParserTest {

    private lateinit var parser: M3uParser

    @Before
    fun setUp() {
        parser = M3uParser()
    }

    @Test
    fun testParseStandardExtinf() {
        val content = """
            #EXTM3U
            #EXTINF:-1 tvg-id="cnn.us" tvg-logo="https://example.com/cnn.png" group-title="News",CNN International
            https://live.example.com/cnn/index.m3u8
        """.trimIndent()

        val channels = parser.parse(content)
        assertEquals(1, channels.size)

        val channel = channels[0]
        assertEquals("CNN International", channel.name)
        assertEquals("https://live.example.com/cnn/index.m3u8", channel.streamUrl)
        assertEquals("News", channel.groupTitle)
        assertEquals("cnn.us", channel.tvgId)
        assertEquals("https://example.com/cnn.png", channel.tvgLogo)
    }

    @Test
    fun testParseMultipleChannelsWithDifferentFormats() {
        val content = """
            #EXTM3U
            #EXTINF:-1 tvg-id="sports.1" group-title="Sports",ESPN 1
            http://stream.example.com/espn.m3u8
            
            #EXTINF:-1 group-title="Cinema" tvg-logo='http://logo.com/movie.png',HBO Movies HD
            http://stream.example.com/hbo.ts
            
            #EXTINF:0,Discovery Channel, USA
            https://stream.example.com/discovery.mp4
        """.trimIndent()

        val channels = parser.parse(content)
        assertEquals(3, channels.size)

        assertEquals("ESPN 1", channels[0].name)
        assertEquals("Sports", channels[0].groupTitle)

        assertEquals("HBO Movies HD", channels[1].name)
        assertEquals("Cinema", channels[1].groupTitle)
        assertEquals("http://logo.com/movie.png", channels[1].tvgLogo)

        assertEquals("Discovery Channel, USA", channels[2].name)
        assertEquals("General", channels[2].groupTitle)
    }

    @Test
    fun testParseEmptyAndMalformedContent() {
        val emptyChannels = parser.parse("")
        assertTrue(emptyChannels.isEmpty())

        val commentOnly = """
            #EXTM3U
            #EXTVLCOPT:http-user-agent=VLC
            # Some random comment
        """.trimIndent()
        val channels = parser.parse(commentOnly)
        assertTrue(channels.isEmpty())
    }

    @Test
    fun testParseDirectUrlsWithoutExtinf() {
        val content = """
            #EXTM3U
            http://stream.example.com/stream1.m3u8
            https://stream.example.com/stream2.m3u8
        """.trimIndent()

        val channels = parser.parse(content)
        assertEquals(2, channels.size)
        assertEquals("http://stream.example.com/stream1.m3u8", channels[0].streamUrl)
        assertEquals("https://stream.example.com/stream2.m3u8", channels[1].streamUrl)
    }

    @Test
    fun testParseTamilTvPlaylistFormat() {
        val content = """
            #EXTM3U
            #EXTINF:-1 tvg-id="KalaignarTV.in" tvg-logo="https://i.imgur.com/logo1.png" group-title="General",Kalaignar TV
            https://stream.example.com/live/kalaignar/index.m3u8
            #EXTINF:-1 tvg-id="SunTV.in" tvg-logo="https://i.imgur.com/logo2.png" group-title="Entertainment",Sun TV
            https://stream.example.com/live/suntv/index.m3u8
            #EXTINF:-1 tvg-id="PolimerNews.in" group-title="News",Polimer News
            https://stream.example.com/live/polimer/index.m3u8
        """.trimIndent()

        val channels = parser.parse(content)
        assertEquals(3, channels.size)
        assertEquals("Kalaignar TV", channels[0].name)
        assertEquals("General", channels[0].groupTitle)
        assertEquals("KalaignarTV.in", channels[0].tvgId)
        assertEquals("Sun TV", channels[1].name)
        assertEquals("Entertainment", channels[1].groupTitle)
        assertEquals("Polimer News", channels[2].name)
        assertEquals("News", channels[2].groupTitle)
    }
}
