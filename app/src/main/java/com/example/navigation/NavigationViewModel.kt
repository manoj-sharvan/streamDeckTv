package com.example.navigation

import androidx.lifecycle.ViewModel
import com.example.NavTab
import com.example.domain.model.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages TV top-level navigation, backstack, and global modal dialog visibility.
 */
class NavigationViewModel : ViewModel() {

    private val navStack = mutableListOf(NavTab.HOME)

    private val _currentTab = MutableStateFlow(NavTab.HOME)
    val currentTab: StateFlow<NavTab> = _currentTab.asStateFlow()

    private val _showExitDialog = MutableStateFlow(false)
    val showExitDialog: StateFlow<Boolean> = _showExitDialog.asStateFlow()

    private val _channelForActions = MutableStateFlow<Channel?>(null)
    val channelForActions: StateFlow<Channel?> = _channelForActions.asStateFlow()

    private val _channelForLanguageEdit = MutableStateFlow<Channel?>(null)
    val channelForLanguageEdit: StateFlow<Channel?> = _channelForLanguageEdit.asStateFlow()

    fun selectTab(tab: NavTab) {
        if (_currentTab.value == tab) return
        navStack.remove(tab)
        navStack.add(tab)
        _currentTab.value = tab
    }

    /**
     * Handles hardware Back button. Returns true if navigation consumed the event,
     * or false if exit confirmation dialog should be displayed.
     */
    fun handleBack(): Boolean {
        if (_showExitDialog.value) {
            _showExitDialog.value = false
            return true
        }

        if (_channelForLanguageEdit.value != null) {
            _channelForLanguageEdit.value = null
            return true
        }

        if (_channelForActions.value != null) {
            _channelForActions.value = null
            return true
        }

        if (navStack.size > 1) {
            navStack.removeAt(navStack.lastIndex)
            _currentTab.value = navStack.last()
            return true
        } else if (_currentTab.value != NavTab.HOME) {
            navStack.clear()
            navStack.add(NavTab.HOME)
            _currentTab.value = NavTab.HOME
            return true
        } else {
            _showExitDialog.value = true
            return true
        }
    }

    fun openExitDialog() {
        _showExitDialog.value = true
    }

    fun dismissExitDialog() {
        _showExitDialog.value = false
    }

    fun openChannelActions(channel: Channel) {
        _channelForActions.value = channel
    }

    fun closeChannelActions() {
        _channelForActions.value = null
    }

    fun openLanguageEdit(channel: Channel) {
        _channelForActions.value = null
        _channelForLanguageEdit.value = channel
    }

    fun closeLanguageEdit() {
        _channelForLanguageEdit.value = null
    }
}
