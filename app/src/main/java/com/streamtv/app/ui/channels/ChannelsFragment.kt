package com.streamtv.app.ui.channels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.streamtv.app.data.model.Channel
import com.streamtv.app.data.model.UiState
import com.streamtv.app.databinding.FragmentChannelsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChannelsFragment : Fragment() {

    private var _binding: FragmentChannelsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChannelsViewModel by viewModels()
    private lateinit var channelAdapter: ChannelAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChannelsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapter()
        setupRecyclerView()
        setupSearch()
        observeViewModel()
    }

    private fun setupAdapter() {
        channelAdapter = ChannelAdapter(
            onChannelClick = ::onChannelClick,
            onFavoriteClick = ::onFavoriteClick,
            layoutType = ChannelAdapter.LAYOUT_LIST
        )
    }

    private fun setupRecyclerView() {
        binding.rvChannels.adapter = channelAdapter
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setSearchQuery(newText ?: "")
                return true
            }
        })
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.channels.collect(::handleChannelsState) }
                launch { viewModel.groups.collect(::setupGroupChips) }
                launch {
                    viewModel.selectedGroup.collect { group ->
                        binding.tvCurrentGroup.text = group ?: "All Channels"
                    }
                }
            }
        }
    }

    private fun setupGroupChips(groups: List<String>) {
        binding.chipGroupCategories.removeAllViews()

        // Add "All" chip
        val allChip = Chip(requireContext()).apply {
            text = "All"
            isCheckable = true
            isChecked = viewModel.selectedGroup.value == null
            setOnClickListener { viewModel.selectGroup(null) }
        }
        binding.chipGroupCategories.addView(allChip)

        // Add group chips
        groups.forEach { group ->
            val chip = Chip(requireContext()).apply {
                text = group
                isCheckable = true
                isChecked = viewModel.selectedGroup.value == group
                setOnClickListener { viewModel.selectGroup(group) }
            }
            binding.chipGroupCategories.addView(chip)
        }
    }

    private fun handleChannelsState(state: UiState<List<Channel>>) {
        binding.apply {
            progressChannels.isVisible = state is UiState.Loading
            emptyView.isVisible = state is UiState.Empty
            rvChannels.isVisible = state is UiState.Success

            if (state is UiState.Success) {
                channelAdapter.submitList(state.data)
                tvChannelCount.text = "${state.data.size} channels"
            }

            if (state is UiState.Error) {
                emptyView.isVisible = true
                tvEmptyMessage.text = state.message
            }
        }
    }

    private fun onChannelClick(channel: Channel) {
        viewModel.onChannelWatched(channel.id)
        findNavController().navigate(
            ChannelsFragmentDirections.actionChannelsToPlayer(channel)
        )
    }

    private fun onFavoriteClick(channel: Channel) {
        viewModel.toggleFavorite(channel)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
