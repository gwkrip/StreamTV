package com.streamtv.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val url: String,
    val isActive: Boolean = true,
    val lastUpdated: Long = System.currentTimeMillis(),
    val channelCount: Int = 0
)
