package com.streamtv.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "channels")
data class ChannelEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val url: String,
    val logoUrl: String?,
    val group: String?,
    val language: String?,
    val country: String?,
    val epgId: String?,
    val isFavorite: Boolean = false,
    val lastWatched: Long? = null,
    val playlistId: Long
)
