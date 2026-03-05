package com.streamtv.app.ui.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.streamtv.app.data.model.Playlist
import com.streamtv.app.databinding.ItemPlaylistBinding
import java.text.SimpleDateFormat
import java.util.*

class PlaylistAdapter(
    private val onRefreshClick: (Playlist) -> Unit,
    private val onDeleteClick: (Playlist) -> Unit
) : ListAdapter<Playlist, PlaylistAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPlaylistBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemPlaylistBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(playlist: Playlist) {
            binding.apply {
                tvPlaylistName.text = playlist.name
                tvChannelCount.text = "${playlist.channelCount} channels"
                tvLastUpdated.text = "Updated: ${formatTimestamp(playlist.lastUpdated)}"

                btnRefresh.setOnClickListener { onRefreshClick(playlist) }
                btnDelete.setOnClickListener { onDeleteClick(playlist) }
            }
        }

        private fun formatTimestamp(timestamp: Long): String {
            val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Playlist>() {
        override fun areItemsTheSame(oldItem: Playlist, newItem: Playlist) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Playlist, newItem: Playlist) =
            oldItem == newItem
    }
}
