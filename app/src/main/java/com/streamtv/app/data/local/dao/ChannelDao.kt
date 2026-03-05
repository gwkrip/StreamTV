package com.streamtv.app.data.local.dao

import androidx.room.*
import com.streamtv.app.data.local.entity.ChannelEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChannelDao {

    @Query("SELECT * FROM channels ORDER BY name ASC")
    fun getAllChannels(): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE playlistId = :playlistId ORDER BY name ASC")
    fun getChannelsByPlaylist(playlistId: Long): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteChannels(): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE lastWatched IS NOT NULL ORDER BY lastWatched DESC LIMIT 20")
    fun getRecentChannels(): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE `group` = :group ORDER BY name ASC")
    fun getChannelsByGroup(group: String): Flow<List<ChannelEntity>>

    @Query("SELECT DISTINCT `group` FROM channels WHERE `group` IS NOT NULL ORDER BY `group` ASC")
    fun getAllGroups(): Flow<List<String>>

    @Query("SELECT * FROM channels WHERE name LIKE '%' || :query || '%' OR `group` LIKE '%' || :query || '%'")
    fun searchChannels(query: String): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE id = :id")
    suspend fun getChannelById(id: Long): ChannelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannel(channel: ChannelEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<ChannelEntity>)

    @Update
    suspend fun updateChannel(channel: ChannelEntity)

    @Query("UPDATE channels SET isFavorite = :isFavorite WHERE id = :channelId")
    suspend fun updateFavoriteStatus(channelId: Long, isFavorite: Boolean)

    @Query("UPDATE channels SET lastWatched = :timestamp WHERE id = :channelId")
    suspend fun updateLastWatched(channelId: Long, timestamp: Long)

    @Delete
    suspend fun deleteChannel(channel: ChannelEntity)

    @Query("DELETE FROM channels WHERE playlistId = :playlistId")
    suspend fun deleteChannelsByPlaylist(playlistId: Long)

    @Query("SELECT COUNT(*) FROM channels WHERE playlistId = :playlistId")
    suspend fun getChannelCountByPlaylist(playlistId: Long): Int
}
