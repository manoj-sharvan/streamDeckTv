package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "streamdeck_tv_prefs")

class AppPreferences(private val context: Context) {

    private object PreferencesKeys {
        val ACTIVE_PLAYLIST_ID = longPreferencesKey("active_playlist_id")
        val LAST_PLAYED_CHANNEL_ID = longPreferencesKey("last_played_channel_id")
        val AUTO_PLAY_LAST_CHANNEL = booleanPreferencesKey("auto_play_last_channel")
        val BUFFER_BOOST_MODE = booleanPreferencesKey("buffer_boost_mode")
    }

    val activePlaylistId: Flow<Long?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ACTIVE_PLAYLIST_ID]
    }

    val lastPlayedChannelId: Flow<Long?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LAST_PLAYED_CHANNEL_ID]
    }

    val autoPlayLastChannel: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.AUTO_PLAY_LAST_CHANNEL] ?: false
    }

    val bufferBoostMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.BUFFER_BOOST_MODE] ?: true
    }

    suspend fun setActivePlaylistId(playlistId: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACTIVE_PLAYLIST_ID] = playlistId
        }
    }

    suspend fun setLastPlayedChannelId(channelId: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_PLAYED_CHANNEL_ID] = channelId
        }
    }

    suspend fun setAutoPlayLastChannel(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_PLAY_LAST_CHANNEL] = enabled
        }
    }

    suspend fun setBufferBoostMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BUFFER_BOOST_MODE] = enabled
        }
    }
}
