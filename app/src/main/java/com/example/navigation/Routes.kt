package com.example.navigation

/**
 * Type-safe navigation routes for StreamDeck TV.
 */
sealed class ScreenRoute(val route: String) {
    object Home : ScreenRoute("home")
    object LiveTv : ScreenRoute("live_tv")
    object Favorites : ScreenRoute("favorites")
    object Playlists : ScreenRoute("playlists")
    object LocalMedia : ScreenRoute("local_media")
    object Settings : ScreenRoute("settings")
    object Player : ScreenRoute("player")
}
