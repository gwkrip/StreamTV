package com.streamtv.app.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Channel(
    val id: Long = 0,
    val name: String,
    val url: String,
    val logoUrl: String? = null,
    val group: String? = null,
    val language: String? = null,
    val country: String? = null,
    val epgId: String? = null,
    val isFavorite: Boolean = false,
    val lastWatched: Long? = null,
    val playlistId: Long = 0
) : Parcelable

data class ChannelGroup(
    val name: String,
    val channels: List<Channel>,
    val channelCount: Int = channels.size
)

data class Playlist(
    val id: Long = 0,
    val name: String,
    val url: String,
    val isActive: Boolean = true,
    val lastUpdated: Long = System.currentTimeMillis(),
    val channelCount: Int = 0
)

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
    object Empty : UiState<Nothing>()
}
