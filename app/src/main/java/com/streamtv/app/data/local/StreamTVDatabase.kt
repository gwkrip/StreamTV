package com.streamtv.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.streamtv.app.data.local.dao.ChannelDao
import com.streamtv.app.data.local.dao.PlaylistDao
import com.streamtv.app.data.local.entity.ChannelEntity
import com.streamtv.app.data.local.entity.PlaylistEntity

@Database(
    entities = [
        ChannelEntity::class,
        PlaylistEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class StreamTVDatabase : RoomDatabase() {
    abstract fun channelDao(): ChannelDao
    abstract fun playlistDao(): PlaylistDao

    companion object {
        const val DATABASE_NAME = "streamtv_db"
    }
}
