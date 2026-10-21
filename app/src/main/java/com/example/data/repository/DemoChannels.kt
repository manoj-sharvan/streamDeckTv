package com.example.data.repository

import com.example.data.parser.ParsedChannel

/**
 * Public domain and CC-BY authorized sample video streams used exclusively
 * for testing UI, ExoPlayer streaming, and D-pad TV navigation.
 * No copyrighted television broadcasts or unauthorized streams are bundled.
 */
object DemoChannels {
    val SAMPLE_PLAYLIST_NAME = "Demo Open Streams"

    val SAMPLE_CHANNELS = listOf(
        ParsedChannel(
            name = "Big Buck Bunny (HLS Stream)",
            streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
            groupTitle = "Animation & Movies",
            tvgId = "demo.bbb",
            tvgLogo = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=300"
        ),
        ParsedChannel(
            name = "Sintel Open Movie (HLS)",
            streamUrl = "https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8",
            groupTitle = "Animation & Movies",
            tvgId = "demo.sintel",
            tvgLogo = "https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=300"
        ),
        ParsedChannel(
            name = "Tears of Steel (Sci-Fi HLS)",
            streamUrl = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8",
            groupTitle = "Sci-Fi & Action",
            tvgId = "demo.tos",
            tvgLogo = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=300"
        ),
        ParsedChannel(
            name = "Akamai Multi-Bitrate Test Stream",
            streamUrl = "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8",
            groupTitle = "News & Broadcast Test",
            tvgId = "demo.akamai",
            tvgLogo = "https://images.unsplash.com/photo-1585829365295-ab7cd400c167?w=300"
        ),
        ParsedChannel(
            name = "Unified Streaming Multi-Audio Demo",
            streamUrl = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.mp4/.m3u8",
            groupTitle = "Sci-Fi & Action",
            tvgId = "demo.audio",
            tvgLogo = "https://images.unsplash.com/photo-1478760329108-5c3ed9d495a0?w=300"
        ),
        ParsedChannel(
            name = "Nature & Wildlife Demo (MP4)",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            groupTitle = "Documentary & Nature",
            tvgId = "demo.blazes",
            tvgLogo = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=300"
        ),
        ParsedChannel(
            name = "Deep Space & Cosmos (MP4)",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
            groupTitle = "Documentary & Nature",
            tvgId = "demo.escapes",
            tvgLogo = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=300"
        ),
        ParsedChannel(
            name = "Tech & Innovation Labs (MP4)",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
            groupTitle = "News & Broadcast Test",
            tvgId = "demo.fun",
            tvgLogo = "https://images.unsplash.com/photo-1519389950473-47ba0277781c?w=300"
        )
    )
}
