package com.streamtv.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamtv.app.data.model.Playlist
import com.streamtv.app.data.model.UiState
import com.streamtv.app.data.repository.ChannelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: ChannelRepository
) : ViewModel() {

    val playlists: StateFlow<UiState<List<Playlist>>> = repository.getAllPlaylists()
        .map { playlists ->
            if (playlists.isEmpty()) UiState.Empty
            else UiState.Success(playlists)
        }
        .catch { emit(UiState.Error(it.message ?: "Unknown error")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    private val _addPlaylistState = MutableStateFlow<UiState<Unit>?>(null)
    val addPlaylistState: StateFlow<UiState<Unit>?> = _addPlaylistState

    private val _refreshState = MutableStateFlow<UiState<Int>?>(null)
    val refreshState: StateFlow<UiState<Int>?> = _refreshState

    fun addPlaylist(name: String, url: String) {
        if (name.isBlank() || url.isBlank()) {
            _addPlaylistState.value = UiState.Error("Name and URL are required")
            return
        }

        viewModelScope.launch {
            _addPlaylistState.value = UiState.Loading
            val result = repository.addPlaylist(name.trim(), url.trim())
            _addPlaylistState.value = result.fold(
                onSuccess = { UiState.Success(Unit) },
                onFailure = { UiState.Error(it.message ?: "Failed to add playlist") }
            )
        }
    }

    fun refreshPlaylist(playlist: Playlist) {
        viewModelScope.launch {
            _refreshState.value = UiState.Loading
            val result = repository.refreshPlaylist(playlist.id, playlist.url)
            _refreshState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Failed to refresh playlist") }
            )
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
        }
    }

    fun clearAddPlaylistState() {
        _addPlaylistState.value = null
    }
}
