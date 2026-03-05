package com.streamtv.app.ui.channels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamtv.app.data.model.Channel
import com.streamtv.app.data.model.ChannelGroup
import com.streamtv.app.data.model.UiState
import com.streamtv.app.data.repository.ChannelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChannelsViewModel @Inject constructor(
    private val repository: ChannelRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedGroup = MutableStateFlow<String?>(null)
    val selectedGroup: StateFlow<String?> = _selectedGroup

    val groups: StateFlow<List<String>> = repository.getAllGroups()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val channels: StateFlow<UiState<List<Channel>>> = combine(
        _searchQuery.debounce(300),
        _selectedGroup
    ) { query, group -> Pair(query, group) }
        .flatMapLatest { (query, group) ->
            when {
                query.isNotBlank() -> repository.searchChannels(query)
                group != null -> repository.getChannelsByGroup(group)
                else -> repository.getAllChannels()
            }.map { channels ->
                if (channels.isEmpty()) UiState.Empty
                else UiState.Success(channels)
            }
        }
        .catch { emit(UiState.Error(it.message ?: "Unknown error")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    val groupedChannels: StateFlow<UiState<List<ChannelGroup>>> = repository.getAllChannels()
        .map { channels ->
            if (channels.isEmpty()) return@map UiState.Empty
            val grouped = channels
                .groupBy { it.group ?: "Uncategorized" }
                .map { (name, chs) -> ChannelGroup(name, chs) }
                .sortedBy { it.name }
            UiState.Success(grouped)
        }
        .catch { emit(UiState.Error(it.message ?: "Unknown error")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isNotBlank()) _selectedGroup.value = null
    }

    fun selectGroup(group: String?) {
        _selectedGroup.value = group
        _searchQuery.value = ""
    }

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
