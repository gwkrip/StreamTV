package com.streamtv.app.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamtv.app.data.model.Channel
import com.streamtv.app.data.repository.ChannelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val repository: ChannelRepository
) : ViewModel() {

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite

    private var currentChannel: Channel? = null

    fun init(channel: Channel) {
        currentChannel = channel
        _isFavorite.value = channel.isFavorite
    }

    fun toggleFavorite(channel: Channel) {
        viewModelScope.launch {
            val newFavoriteState = !_isFavorite.value
            _isFavorite.value = newFavoriteState
            repository.toggleFavorite(channel.id, newFavoriteState)
        }
    }
}
