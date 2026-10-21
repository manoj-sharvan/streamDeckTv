# StreamDeck TV — Native Android IPTV Player for Android TV & Google TV

StreamDeck TV is a native Android television application built using **Kotlin**, **Jetpack Compose**, and **AndroidX Media3 ExoPlayer**, engineered specifically for 10-foot television screens and D-pad remote controllers.

---

## Features

1. **TV-First Design & Remote Navigation**
   - High-contrast charcoal dark interface (`#0D0F15`) with neon cyan/sky blue remote focus borders.
   - 100% operable with standard Android TV / Google TV remote controls (D-Pad Up/Down/Left/Right, Select, Back, Play/Pause).
   - Dynamic animated focus scaling and glowing borders on every focusable card and button.

2. **Default & Custom Playlist Management**
   - **Built-in Tamil TV Default**: Pre-configured with Tamil TV (`https://iptv-org.github.io/iptv/languages/tam.m3u`). Automatically loaded and cached on first startup.
   - **Local Cache & Offline Mode**: Channels are cached in SQLite via Room. When offline, cached metadata is rendered alongside an offline badge without blocking navigation.
   - **My Playlists Screen**: Dedicated TV management screen showing Tamil TV first, followed by custom playlists. Users can switch, refresh, rename, and delete custom playlists (deletion of built-in Tamil TV is protected).
   - **Duplicate Detection**: Adding an existing playlist URL automatically updates and switches to the existing record rather than creating duplicates.
   - **Favorite Preservation**: When refreshing or re-importing a playlist, all user-favorited channels are preserved.
   - **Remote Web Import**: Validates HTTP/HTTPS M3U/M3U8 URLs with coroutine-based asynchronous downloads and error diagnostics.
   - **Local File Import**: Select local `.m3u` or `.m3u8` playlist files using Android Storage Access Framework (SAF).
   - **Demo Channels**: Instant 1-click loading of public domain and Creative Commons authorized test streams (Big Buck Bunny, Sintel, Tears of Steel, Akamai multi-bitrate HLS).

3. **Live TV Guide & Categorization**
   - Horizontal category chips extracted from playlist metadata.
   - Quick search by channel title or category group.
   - Instant channel switching with one-touch playback.
   - Channel favorite pinning with dedicated Favorites screen.

4. **Media3 ExoPlayer Video Player**
   - Fullscreen playback with HLS adaptive bitrate streaming support (`.m3u8`).
   - Dynamic detection of live streams vs seekable media (`player.isCurrentMediaItemLive`).
   - Audio track selector dialog for multi-language broadcasts.
   - Embedded subtitle / closed captions selector.
   - Comprehensive playback error handling with user-friendly diagnosis and one-touch retry.

5. **Persistence & Offline Local Media**
   - **Room Database**: Local SQLite caching for playlists, channels, favorites, and playback history.
   - **DataStore Preferences**: Persists active playlist selection, last tuned channel, and playback settings.
   - **Offline Local Media Player**: Select and play local videos (MP4, MKV, WebM, TS) stored on internal TV memory or attached USB drives via SAF.

---

## Architecture Overview

StreamDeck TV adheres to clean architecture principles:

```
com.example/
├── data/
│   ├── local/          # Room Entities, DAOs, StreamDeckDatabase, AppPreferences (DataStore)
│   ├── remote/         # OkHttp PlaylistDownloader
│   ├── parser/         # M3uParser for #EXTM3U and #EXTINF directives
│   └── repository/     # PlaylistRepository, ChannelRepository, DemoChannels
├── domain/
│   └── model/          # Channel, Playlist, PlaybackHistory, TrackInfo
├── feature/
│   ├── home/           # TV dashboard, quick watch, recent channels, empty state
│   ├── live/           # Live TV guide, categories, search, channel grid
│   ├── favorites/      # Pinned favorite channels
│   ├── localmedia/     # Offline video player with Storage Access Framework
│   ├── playlist/       # Import playlist modal (URL / SAF file / Demo)
│   └── settings/       # Playlist management, TV remote reference guide, disclaimers
├── navigation/         # TvNavigationRail for landscape TV layout
├── player/             # StreamPlayerManager (Media3 ExoPlayer wrapper)
└── ui/
    ├── components/     # TvFocusableCard, TvFocusableButton, TvChannelCard, TvCategoryChip
    └── theme/          # Charcoal TV theme & high-contrast focus colors
```

---

## Android Studio Steps to Build and Test on Android TV

To produce a tested Android TV APK or run the app on an Android TV emulator/device:

1. **Open the Project in Android Studio**:
   - Launch **Android Studio Ladybug (or Hedgehog / Iguana / Jellyfish / Meerkat)**.
   - Select **Open** and choose this project root folder.
   - Allow Gradle to sync dependencies (`gradle sync`).

2. **Create or Launch an Android TV Emulator**:
   - In Android Studio, open **Device Manager** (`Tools` > `Device Manager`).
   - Click **Create Device** and select the **TV** category.
   - Choose a TV hardware profile such as:
     - **Android TV (1080p)** or **Google TV (1080p)**.
   - Select a system image: **Android 14 (API 34)** or **Android 15 / 16 (API 35/36)** with Google APIs.
   - Click **Finish** and start the emulator.

3. **Run the Application**:
   - Select the `app` run configuration in the top toolbar.
   - Select your Android TV emulator or connected physical Android TV device (enabled with ADB over Wi-Fi / USB).
   - Press **Run** (`Shift + F10`).
   - The app will appear on the Android TV Leanback launcher home screen under "StreamDeck TV" with its custom 320x180 TV banner.

4. **Navigate Using the TV Remote**:
   - In the Android Studio emulator tool window, use the **Directional Pad (D-pad)** buttons:
     - **Up / Down**: Scroll through categories and channel rows.
     - **Left / Right**: Move between sidebar navigation, search bar, and channel cards.
     - **Center Button (Select / Enter)**: Tune channels, click buttons.
     - **Back**: Exit fullscreen player back to live guide.

---

## Permissions & Manifest Configuration

- `android.permission.INTERNET`: Required to fetch remote M3U playlists and stream video over HTTP/HTTPS.
- `android.permission.ACCESS_NETWORK_STATE`: Used to verify connectivity before streaming.
- `android.software.leanback` (`android:required="false"`): Declared so the app is recognized by Android TV and Google TV while maintaining flexibility.
- `android.hardware.touchscreen` (`android:required="false"`): Indicates touch is not mandatory, enabling deployment to TV devices without touchscreens.
- `android.intent.category.LEANBACK_LAUNCHER`: Ensures StreamDeck TV appears on the Android TV launcher carousel.

---

## Supported Media Formats (Media3 ExoPlayer)

- **Streaming Protocols**: HLS (`.m3u8`), HTTP progressive streaming.
- **Video Containers**: MP4, MKV, WebM, MPEG-TS (`.ts`).
- **Video Codecs**: H.264 (AVC), H.265 (HEVC), VP9, AV1 (hardware-dependent).
- **Audio Codecs**: AAC, MP3, AC3, E-AC3 (Dolby Digital Plus), Opus, Vorbis.
- **Subtitles**: SubRip (`.srt`), WebVTT (`.vtt`), embedded CEA-608 / CEA-708 captions.

---

## Legal & Compliance Notice

StreamDeck TV is an open-source client media player. It does not provide, host, or bundle any copyrighted television broadcasts, IPTV subscriptions, or unauthorized streams. The bundled demo streams are public domain and Creative Commons licensed samples provided solely for interface testing and verification.
