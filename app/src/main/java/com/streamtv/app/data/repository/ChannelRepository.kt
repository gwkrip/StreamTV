package com.streamtv.app.data.repository

import com.streamtv.app.data.local.dao.ChannelDao
import com.streamtv.app.data.local.dao.PlaylistDao
import com.streamtv.app.data.local.entity.ChannelEntity
import com.streamtv.app.data.local.entity.PlaylistEntity
import com.streamtv.app.data.model.Channel
import com.streamtv.app.data.model.Playlist
import com.streamtv.app.data.remote.M3uParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChannelRepository @Inject constructor(
    private val channelDao: ChannelDao,
    private val playlistDao: PlaylistDao,
    private val m3uParser: M3uParser
) {

    // --- Channels ---

    fun getAllChannels(): Flow<List<Channel>> =
        channelDao.getAllChannels().map { it.map(::entityToChannel) }

    fun getFavoriteChannels(): Flow<List<Channel>> =
        channelDao.getFavoriteChannels().map { it.map(::entityToChannel) }

    fun getRecentChannels(): Flow<List<Channel>> =
        channelDao.getRecentChannels().map { it.map(::entityToChannel) }

    fun getChannelsByGroup(group: String): Flow<List<Channel>> =
        channelDao.getChannelsByGroup(group).map { it.map(::entityToChannel) }

    fun getAllGroups(): Flow<List<String>> = channelDao.getAllGroups()

    fun searchChannels(query: String): Flow<List<Channel>> =
        channelDao.searchChannels(query).map { it.map(::entityToChannel) }

    suspend fun toggleFavorite(channelId: Long, isFavorite: Boolean) {
        channelDao.updateFavoriteStatus(channelId, isFavorite)
    }

    suspend fun updateLastWatched(channelId: Long) {
        channelDao.updateLastWatched(channelId, System.currentTimeMillis())
    }

    // --- Playlists ---

    fun getAllPlaylists(): Flow<List<Playlist>> =
        playlistDao.getAllPlaylists().map { it.map(::entityToPlaylist) }

    suspend fun addPlaylist(name: String, url: String): Result<Long> {
        return try {
            val playlist = PlaylistEntity(name = name, url = url)
            val id = playlistDao.insertPlaylist(playlist)
            refreshPlaylist(id, url)
            Result.success(id)
        } catch (e: Exception) {
            Timber.e(e, "Error adding playlist")
            Result.failure(e)
        }
    }

    suspend fun refreshPlaylist(playlistId: Long, url: String): Result<Int> {
        return try {
            val result = m3uParser.parseFromUrl(url, playlistId)
            result.fold(
                onSuccess = { channels ->
                    // Delete old channels
                    channelDao.deleteChannelsByPlaylist(playlistId)
                    // Insert new channels
                    channelDao.insertChannels(channels.map(::channelToEntity))
                    // Update playlist stats
                    playlistDao.updatePlaylistStats(
                        playlistId,
                        channels.size,
                        System.currentTimeMillis()
                    )
                    Timber.d("Refreshed playlist $playlistId with ${channels.size} channels")
                    Result.success(channels.size)
                },
                onFailure = { Result.failure(it) }
            )
        } catch (e: Exception) {
            Timber.e(e, "Error refreshing playlist")
            Result.failure(e)
        }
    }

    suspend fun deletePlaylist(playlistId: Long) {
        val playlist = playlistDao.getPlaylistById(playlistId) ?: return
        channelDao.deleteChannelsByPlaylist(playlistId)
        playlistDao.deletePlaylist(playlist)
    }

    // --- Mappers ---

    private fun entityToChannel(entity: ChannelEntity) = Channel(
        id = entity.id,
        name = entity.name,
        url = entity.url,
        logoUrl = entity.logoUrl,
        group = entity.group,
        language = entity.language,
        country = entity.country,
        epgId = entity.epgId,
        isFavorite = entity.isFavorite,
        lastWatched = entity.lastWatched,
        playlistId = entity.playlistId
    )

    private fun channelToEntity(channel: Channel) = ChannelEntity(
        id = channel.id,
        name = channel.name,
        url = channel.url,
        logoUrl = channel.logoUrl,
        group = channel.group,
        language = channel.language,
        country = channel.country,
        epgId = channel.epgId,
        isFavorite = channel.isFavorite,
        lastWatched = channel.lastWatched,
        playlistId = channel.playlistId
    )

    private fun entityToPlaylist(entity: PlaylistEntity) = Playlist(
        id = entity.id,
        name = entity.name,
        url = entity.url,
        isActive = entity.isActive,
        lastUpdated = entity.lastUpdated,
        channelCount = entity.channelCount
    )
}
