package com.example

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import kotlinx.coroutines.delay
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.feature.channel.ChannelActionDialog
import com.example.feature.channel.LanguageSelectDialog
import com.example.feature.common.ConfirmActionDialog
import com.example.feature.common.ExitConfirmationDialog
import com.example.feature.favorites.FavoritesScreen
import com.example.feature.home.HomeScreen
import com.example.feature.live.LiveTvScreen
import com.example.feature.localmedia.LocalMediaScreen
import com.example.feature.player.PlayerScreen
import com.example.feature.playlist.AddPlaylistDialog
import com.example.feature.playlists.PlaylistsScreen
import com.example.feature.settings.SettingsScreen
import com.example.navigation.TvNavigationRail
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TvBackground
import com.example.di.AppViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        AppViewModelFactory((application as StreamDeckTvApp).appContainer)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppContent(
                    viewModel = viewModel,
                    onExitApp = {
                        viewModel.closePlayer()
                        finish()
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        (application as StreamDeckTvApp).appContainer.playerManager.stop()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
            KeyEvent.KEYCODE_HEADSETHOOK -> {
                (application as StreamDeckTvApp).appContainer.playerManager.togglePlayPause()
                true
            }
            KeyEvent.KEYCODE_MEDIA_PLAY -> {
                (application as StreamDeckTvApp).appContainer.playerManager.getPlayer().play()
                true
            }
            KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                (application as StreamDeckTvApp).appContainer.playerManager.getPlayer().pause()
                true
            }
            KeyEvent.KEYCODE_MEDIA_NEXT,
            KeyEvent.KEYCODE_CHANNEL_UP -> {
                viewModel.playNextChannel()
                true
            }
            KeyEvent.KEYCODE_MEDIA_PREVIOUS,
            KeyEvent.KEYCODE_CHANNEL_DOWN -> {
                viewModel.playPreviousChannel()
                true
            }
            KeyEvent.KEYCODE_MENU -> {
                // TV remote menu key: open channel actions or let Compose handle focused item
                val current = viewModel.playerUiState.value.currentChannel
                if (current != null) {
                    viewModel.openChannelActions(current)
                    true
                } else {
                    super.onKeyDown(keyCode, event)
                }
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }
}

@Composable
fun MainAppContent(
    viewModel: MainViewModel,
    onExitApp: () -> Unit
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val activePlaylist by viewModel.activePlaylist.collectAsStateWithLifecycle()
    val activePlaylistId by viewModel.activePlaylistId.collectAsStateWithLifecycle()
    val channels by viewModel.channels.collectAsStateWithLifecycle()
    val languages by viewModel.languages.collectAsStateWithLifecycle()
    val contentCategories by viewModel.contentCategories.collectAsStateWithLifecycle()
    val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val selectedContentCategory by viewModel.selectedContentCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val recentChannels by viewModel.recentChannels.collectAsStateWithLifecycle()
    val playerUiState by viewModel.playerUiState.collectAsStateWithLifecycle()
    val importStatus by viewModel.importStatus.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val refreshingPlaylistId by viewModel.refreshingPlaylistId.collectAsStateWithLifecycle()
    val isAppInitializing by viewModel.isAppInitializing.collectAsStateWithLifecycle()
    val isChannelsLoading by viewModel.isChannelsLoading.collectAsStateWithLifecycle()
    val startupStatusMessage by viewModel.startupStatusMessage.collectAsStateWithLifecycle()

    // Dialog state flows
    val channelForActions by viewModel.channelForActions.collectAsStateWithLifecycle()
    val channelForLanguageEdit by viewModel.channelForLanguageEdit.collectAsStateWithLifecycle()
    val showExitDialog by viewModel.showExitDialog.collectAsStateWithLifecycle()
    val showClearHistoryDialog by viewModel.showClearHistoryDialog.collectAsStateWithLifecycle()
    val showClearCacheDialog by viewModel.showClearCacheDialog.collectAsStateWithLifecycle()

    var isAddPlaylistDialogOpen by remember { mutableStateOf(false) }

    // Preserved Scroll States across tabs and player playback
    val homeListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    val liveTvGridState = rememberSaveable(saver = LazyGridState.Saver) { LazyGridState() }
    val favoritesGridState = rememberSaveable(saver = LazyGridState.Saver) { LazyGridState() }
    val playlistsListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    val settingsListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }

    val contentFocusRequester = remember { FocusRequester() }
    val railFocusRequester = remember { FocusRequester() }

    val isPlayerActive = playerUiState.currentChannel != null

    // Request initial focus on the rail when app finishes initializing
    LaunchedEffect(isAppInitializing) {
        if (!isAppInitializing) {
            delay(150)
            try {
                railFocusRequester.requestFocus()
            } catch (_: Exception) {}
        }
    }

    // Automatically restore focus to channel catalog when returning from player
    LaunchedEffect(isPlayerActive) {
        if (!isPlayerActive) {
            delay(120)
            try {
                contentFocusRequester.requestFocus()
            } catch (_: Exception) {
                try {
                    railFocusRequester.requestFocus()
                } catch (_: Exception) {}
            }
        }
    }

    // Complete TV Back Navigation Handler
    BackHandler(enabled = true) {
        if (isAddPlaylistDialogOpen) {
            isAddPlaylistDialogOpen = false
            viewModel.clearImportStatus()
        } else {
            val handled = viewModel.handleBackPress()
            if (!handled) {
                onExitApp()
            }
        }
    }

    if (isAppInitializing) {
        com.example.ui.components.TvStartupLoadingScreen(
            statusMessage = startupStatusMessage,
            subStatusMessage = "Connecting to TV services and loading channels…"
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(TvBackground)
        ) {
        // TV Landscape Layout: Left Navigation Rail + Screen Content (retained across player transitions)
        Row(
            modifier = Modifier
                .fillMaxSize()
                .focusProperties { canFocus = !isPlayerActive }
        ) {
            TvNavigationRail(
                currentTab = currentTab,
                onTabSelected = { viewModel.selectTab(it) },
                onNavigateToContent = {
                    try {
                        contentFocusRequester.requestFocus()
                    } catch (_: Exception) {}
                },
                selectedTabFocusRequester = railFocusRequester
            )

            Box(modifier = Modifier.weight(1f)) {
                when (currentTab) {
                    NavTab.HOME -> {
                        HomeScreen(
                            listState = homeListState,
                            playlists = playlists,
                            activePlaylist = activePlaylist,
                            channels = channels,
                            recentChannels = recentChannels,
                            favorites = favorites,
                            languages = languages,
                            contentCategories = contentCategories,
                            isOnline = isOnline,
                            isRefreshing = refreshingPlaylistId == activePlaylistId,
                            isChannelsLoading = isChannelsLoading,
                            contentFocusRequester = contentFocusRequester,
                            onNavigateToRail = {
                                try {
                                    railFocusRequester.requestFocus()
                                } catch (_: Exception) {}
                            },
                            onSelectPlaylist = { viewModel.selectPlaylist(it) },
                            onChannelClick = { viewModel.playChannel(it) },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onChannelOptions = { viewModel.openChannelActions(it) },
                            onRefreshActivePlaylist = { viewModel.refreshActivePlaylist() },
                            onOpenAddPlaylist = { isAddPlaylistDialogOpen = true },
                            onLoadDemoChannels = { viewModel.loadDemoChannels() },
                            onNavigateToLiveTv = { viewModel.selectTab(NavTab.LIVE_TV) },
                            onNavigateToFavorites = { viewModel.selectTab(NavTab.FAVORITES) },
                            onNavigateToPlaylists = { viewModel.selectTab(NavTab.PLAYLISTS) },
                            onNavigateToLocalMedia = { viewModel.selectTab(NavTab.LOCAL_MEDIA) },
                            onNavigateToSettings = { viewModel.selectTab(NavTab.SETTINGS) },
                            onSelectLanguageAndNavigate = { lang ->
                                viewModel.selectLanguage(lang)
                                viewModel.selectTab(NavTab.LIVE_TV)
                            },
                            onSelectCategoryAndNavigate = { cat ->
                                viewModel.selectContentCategory(cat)
                                viewModel.selectTab(NavTab.LIVE_TV)
                            }
                        )
                    }

                    NavTab.LIVE_TV -> {
                        LiveTvScreen(
                            gridState = liveTvGridState,
                            channels = channels,
                            languages = languages,
                            selectedLanguage = selectedLanguage,
                            contentCategories = contentCategories,
                            selectedContentCategory = selectedContentCategory,
                            searchQuery = searchQuery,
                            isOnline = isOnline,
                            isChannelsLoading = isChannelsLoading,
                            contentFocusRequester = contentFocusRequester,
                            onNavigateToRail = {
                                try {
                                    railFocusRequester.requestFocus()
                                } catch (_: Exception) {}
                            },
                            onLanguageSelect = { viewModel.selectLanguage(it) },
                            onContentCategorySelect = { viewModel.selectContentCategory(it) },
                            onSearchQueryChange = { viewModel.setSearchQuery(it) },
                            onChannelClick = { viewModel.playChannel(it) },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onChannelOptions = { viewModel.openChannelActions(it) }
                        )
                    }

                    NavTab.FAVORITES -> {
                        FavoritesScreen(
                            gridState = favoritesGridState,
                            favorites = favorites,
                            contentFocusRequester = contentFocusRequester,
                            onNavigateToRail = {
                                try {
                                    railFocusRequester.requestFocus()
                                } catch (_: Exception) {}
                            },
                            onChannelClick = { viewModel.playChannel(it) },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onChannelOptions = { viewModel.openChannelActions(it) }
                        )
                    }

                    NavTab.PLAYLISTS -> {
                        PlaylistsScreen(
                            listState = playlistsListState,
                            playlists = playlists,
                            activePlaylistId = activePlaylistId,
                            refreshingPlaylistId = refreshingPlaylistId,
                            contentFocusRequester = contentFocusRequester,
                            onNavigateToRail = {
                                try {
                                    railFocusRequester.requestFocus()
                                } catch (_: Exception) {}
                            },
                            onSelectPlaylist = { viewModel.selectPlaylist(it) },
                            onRefreshPlaylist = { viewModel.refreshPlaylist(it) },
                            onRenamePlaylist = { id, name -> viewModel.renamePlaylist(id, name) },
                            onDeletePlaylist = { viewModel.deletePlaylist(it) },
                            onOpenAddPlaylist = { isAddPlaylistDialogOpen = true }
                        )
                    }

                    NavTab.LOCAL_MEDIA -> {
                        LocalMediaScreen(
                            contentFocusRequester = contentFocusRequester,
                            onNavigateToRail = {
                                try {
                                    railFocusRequester.requestFocus()
                                } catch (_: Exception) {}
                            },
                            onPlayLocalFile = { uri, fileName ->
                                viewModel.playLocalFile(uri, fileName)
                            }
                        )
                    }

                    NavTab.SETTINGS -> {
                        SettingsScreen(
                            listState = settingsListState,
                            playlists = playlists,
                            activePlaylistId = activePlaylistId,
                            contentFocusRequester = contentFocusRequester,
                            onNavigateToRail = {
                                try {
                                    railFocusRequester.requestFocus()
                                } catch (_: Exception) {}
                            },
                            onSelectPlaylist = { viewModel.selectPlaylist(it) },
                            onDeletePlaylist = { viewModel.deletePlaylist(it) },
                            onAddPlaylistClick = { isAddPlaylistDialogOpen = true },
                            onClearPlaybackHistoryClick = { viewModel.showClearHistoryDialog() },
                            onClearCacheClick = { viewModel.showClearCacheDialog() }
                        )
                    }
                }
            }
        }

        // Fullscreen Player overlay when actively watching a channel/video
        if (isPlayerActive) {
            PlayerScreen(
                playerManager = viewModel.playerManager,
                uiState = playerUiState,
                onNextChannel = { viewModel.playNextChannel() },
                onPreviousChannel = { viewModel.playPreviousChannel() },
                onBack = { viewModel.closePlayer() }
            )
        }

        // Add Custom Playlist Modal Dialog
        if (isAddPlaylistDialogOpen) {
            AddPlaylistDialog(
                importStatus = importStatus,
                onImportUrl = { url, name ->
                    viewModel.importFromUrl(url, name)
                },
                onImportUri = { uri, name ->
                    viewModel.importFromUri(uri, name)
                },
                onLoadDemo = {
                    viewModel.loadDemoChannels()
                },
                onDismiss = {
                    viewModel.clearImportStatus()
                    isAddPlaylistDialogOpen = false
                }
            )
        }

        // Channel Actions Modal Dialog (Play, Favorite, Change Language, Close)
        channelForActions?.let { channel ->
            ChannelActionDialog(
                channel = channel,
                onPlay = {
                    viewModel.playChannel(channel)
                },
                onToggleFavorite = {
                    viewModel.toggleFavorite(channel)
                },
                onChangeLanguage = {
                    viewModel.openLanguageEdit(channel)
                },
                onDismiss = {
                    viewModel.closeChannelActions()
                }
            )
        }

        // Language Select Modal Dialog
        channelForLanguageEdit?.let { channel ->
            LanguageSelectDialog(
                channel = channel,
                onSelectLanguage = { newLang ->
                    viewModel.updateChannelLanguage(channel.id, newLang)
                },
                onDismiss = {
                    viewModel.closeLanguageEdit()
                }
            )
        }

        // Exit Application Confirmation Dialog (Back on Home screen)
        if (showExitDialog) {
            ExitConfirmationDialog(
                onConfirmExit = onExitApp,
                onDismiss = { viewModel.dismissExitDialog() }
            )
        }

        // Clear Playback History Confirmation Dialog
        if (showClearHistoryDialog) {
            ConfirmActionDialog(
                title = "Clear Playback History?",
                message = "Are you sure you want to clear your recently watched channels history? Your playlists and favorite channels will not be affected.",
                confirmText = "Clear History",
                cancelText = "Keep History",
                isDestructive = true,
                icon = Icons.Default.History,
                onConfirm = { viewModel.clearPlaybackHistory() },
                onDismiss = { viewModel.dismissClearHistoryDialog() }
            )
        }

        // Clear Channel Cache Confirmation Dialog
        if (showClearCacheDialog) {
            ConfirmActionDialog(
                title = "Clear Channel Cache?",
                message = "This will remove all locally cached channels and re-synchronize from your active playlists. Favorites and manual language overrides are preserved.",
                confirmText = "Clear & Re-sync",
                cancelText = "Cancel",
                isDestructive = true,
                icon = Icons.Default.Delete,
                onConfirm = { viewModel.clearCachedChannels() },
                onDismiss = { viewModel.dismissClearCacheDialog() }
            )
        }
    }
}
}
