package com.streamtv.app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.streamtv.app.data.model.Channel
import com.streamtv.app.data.model.UiState
import com.streamtv.app.databinding.FragmentFavoritesBinding
import com.streamtv.app.ui.channels.ChannelAdapter
import com.streamtv.app.ui.player.PlayerActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var adapter: ChannelAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = ChannelAdapter(
            onChannelClick = ::onChannelClick,
            onFavoriteClick = { viewModel.toggleFavorite(it) },
            layoutType = ChannelAdapter.LAYOUT_LIST
        )
        binding.rvFavorites.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.favoriteChannels.collect { state ->
                    binding.progressFavorites.isVisible = state is UiState.Loading
                    binding.emptyView.isVisible = state is UiState.Empty
                    binding.rvFavorites.isVisible = state is UiState.Success
                    if (state is UiState.Success) adapter.submitList(state.data)
                }
            }
        }
    }

    private fun onChannelClick(channel: Channel) {
        viewModel.onChannelWatched(channel.id)
        PlayerActivity.start(requireContext(), channel)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
