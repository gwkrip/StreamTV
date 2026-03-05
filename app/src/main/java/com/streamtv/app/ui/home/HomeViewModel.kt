package com.streamtv.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamtv.app.data.model.Channel
import com.streamtv.app.data.model.UiState
import com.streamtv.app.data.repository.ChannelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ChannelRepository
) : ViewModel() {

    val recentChannels: StateFlow<UiState<List<Channel>>> = repository.getRecentChannels()
        .map { channels ->
            if (channels.isEmpty()) UiState.Empty
            else UiState.Success(channels)
        }
        .catch { emit(UiState.Error(it.message ?: "Unknown error")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    val favoriteChannels: StateFlow<UiState<List<Channel>>> = repository.getFavoriteChannels()
        .map { channels ->
            if (channels.isEmpty()) UiState.Empty
            else UiState.Success(channels)
        }
        .catch { emit(UiState.Error(it.message ?: "Unknown error")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    fun toggleFavorite(channel: Channel) {
        viewModelScope.launch {
            repository.toggleFavorite(channel.id, !channel.isFavorite)
        }
    }

    fun onChannelWatched(channelId: Long) {
        viewModelScope.launch {
            repository.updateLastWatched(channelId)
        }
    }
}
