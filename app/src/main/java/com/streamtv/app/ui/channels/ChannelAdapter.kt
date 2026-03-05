package com.streamtv.app.ui.channels

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.streamtv.app.R
import com.streamtv.app.data.model.Channel
import com.streamtv.app.databinding.ItemChannelCardBinding
import com.streamtv.app.databinding.ItemChannelListBinding

class ChannelAdapter(
    private val onChannelClick: (Channel) -> Unit,
    private val onFavoriteClick: (Channel) -> Unit,
    val layoutType: Int = LAYOUT_LIST
) : ListAdapter<Channel, RecyclerView.ViewHolder>(ChannelDiffCallback()) {

    companion object {
        const val LAYOUT_LIST = 0
        const val LAYOUT_CARD = 1
    }

    override fun getItemViewType(position: Int): Int = layoutType

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            LAYOUT_CARD -> {
                val binding = ItemChannelCardBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                CardViewHolder(binding)
            }
            else -> {
                val binding = ItemChannelListBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                ListViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val channel = getItem(position)
        when (holder) {
            is CardViewHolder -> holder.bind(channel)
            is ListViewHolder -> holder.bind(channel)
        }
    }

    inner class ListViewHolder(
        private val binding: ItemChannelListBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(channel: Channel) {
            binding.apply {
                tvChannelName.text = channel.name
                tvChannelGroup.text = channel.group ?: "Unknown"

                Glide.with(ivChannelLogo)
                    .load(channel.logoUrl)
                    .placeholder(R.drawable.ic_channel_placeholder)
                    .error(R.drawable.ic_channel_placeholder)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .circleCrop()
                    .into(ivChannelLogo)

                val favoriteIcon = if (channel.isFavorite)
                    R.drawable.ic_favorite_filled
                else
                    R.drawable.ic_favorite_outline

                btnFavorite.setImageResource(favoriteIcon)
                btnFavorite.setColorFilter(
                    ContextCompat.getColor(
                        root.context,
                        if (channel.isFavorite) R.color.accent_red else R.color.text_secondary
                    )
                )

                root.setOnClickListener { onChannelClick(channel) }
                btnFavorite.setOnClickListener { onFavoriteClick(channel) }
            }
        }
    }

    inner class CardViewHolder(
        private val binding: ItemChannelCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(channel: Channel) {
            binding.apply {
                tvChannelName.text = channel.name

                Glide.with(ivChannelLogo)
                    .load(channel.logoUrl)
                    .placeholder(R.drawable.ic_channel_placeholder)
                    .error(R.drawable.ic_channel_placeholder)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(ivChannelLogo)

                root.setOnClickListener { onChannelClick(channel) }
            }
        }
    }

    class ChannelDiffCallback : DiffUtil.ItemCallback<Channel>() {
        override fun areItemsTheSame(oldItem: Channel, newItem: Channel) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Channel, newItem: Channel) =
            oldItem == newItem
    }
}
