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
import androidx.navigation.fragment.findNavController
import com.streamtv.app.R
import com.streamtv.app.data.model.Channel
import com.streamtv.app.data.model.UiState
import com.streamtv.app.databinding.FragmentHomeBinding
import com.streamtv.app.ui.channels.ChannelAdapter
import com.streamtv.app.ui.player.PlayerActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var recentAdapter: ChannelAdapter
    private lateinit var favoriteAdapter: ChannelAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapters()
        setupRecyclerViews()
        observeViewModel()
        setupClickListeners()
    }

    private fun setupAdapters() {
        recentAdapter = ChannelAdapter(
            onChannelClick = ::onChannelClick,
            onFavoriteClick = ::onFavoriteClick,
            layoutType = ChannelAdapter.LAYOUT_CARD
        )
        favoriteAdapter = ChannelAdapter(
            onChannelClick = ::onChannelClick,
            onFavoriteClick = ::onFavoriteClick,
            layoutType = ChannelAdapter.LAYOUT_CARD
        )
    }

    private fun setupRecyclerViews() {
        binding.rvRecentChannels.adapter = recentAdapter
        binding.rvFavoriteChannels.adapter = favoriteAdapter
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.recentChannels.collect { state ->
                        handleRecentState(state)
                    }
                }
                launch {
                    viewModel.favoriteChannels.collect { state ->
                        handleFavoriteState(state)
                    }
                }
            }
        }
    }

    private fun handleRecentState(state: UiState<List<Channel>>) {
        binding.apply {
            progressRecent.isVisible = state is UiState.Loading
            tvNoRecent.isVisible = state is UiState.Empty
            rvRecentChannels.isVisible = state is UiState.Success
            sectionRecent.isVisible = state is UiState.Success

            if (state is UiState.Success) {
                recentAdapter.submitList(state.data)
            }
        }
    }

    private fun handleFavoriteState(state: UiState<List<Channel>>) {
        binding.apply {
            tvNoFavorites.isVisible = state is UiState.Empty
            rvFavoriteChannels.isVisible = state is UiState.Success
            sectionFavorites.isVisible = state !is UiState.Loading

            if (state is UiState.Success) {
                favoriteAdapter.submitList(state.data)
            }
        }
    }

    private fun onChannelClick(channel: Channel) {
        viewModel.onChannelWatched(channel.id)
        PlayerActivity.start(requireContext(), channel)
    }

    private fun onFavoriteClick(channel: Channel) {
        viewModel.toggleFavorite(channel)
    }

    private fun setupClickListeners() {
        binding.btnAddPlaylist.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_settings)
        }
        binding.btnBrowseAll.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_channels)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
