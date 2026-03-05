package com.streamtv.app.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.streamtv.app.R
import com.streamtv.app.data.model.Playlist
import com.streamtv.app.data.model.UiState
import com.streamtv.app.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModels()
    private lateinit var playlistAdapter: PlaylistAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapter()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupAdapter() {
        playlistAdapter = PlaylistAdapter(
            onRefreshClick = { viewModel.refreshPlaylist(it) },
            onDeleteClick = { showDeleteConfirmation(it) }
        )
    }

    private fun setupRecyclerView() {
        binding.rvPlaylists.adapter = playlistAdapter
    }

    private fun setupClickListeners() {
        binding.fabAddPlaylist.setOnClickListener { showAddPlaylistDialog() }
    }

    private fun showAddPlaylistDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_playlist, null)

        val etName = dialogView.findViewById<TextInputEditText>(R.id.et_playlist_name)
        val etUrl = dialogView.findViewById<TextInputEditText>(R.id.et_playlist_url)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add Playlist")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = etName.text?.toString() ?: ""
                val url = etUrl.text?.toString() ?: ""
                viewModel.addPlaylist(name, url)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmation(playlist: Playlist) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Playlist")
            .setMessage("Are you sure you want to delete '${playlist.name}'? This will remove all associated channels.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deletePlaylist(playlist.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.playlists.collect { state ->
                        binding.progressPlaylists.isVisible = state is UiState.Loading
                        binding.emptyPlaylistView.isVisible = state is UiState.Empty
                        binding.rvPlaylists.isVisible = state is UiState.Success

                        if (state is UiState.Success) {
                            playlistAdapter.submitList(state.data)
                        }
                    }
                }

                launch {
                    viewModel.addPlaylistState.collect { state ->
                        when (state) {
                            is UiState.Loading -> binding.progressAdd.isVisible = true
                            is UiState.Success -> {
                                binding.progressAdd.isVisible = false
                                Toast.makeText(requireContext(), "Playlist added successfully!", Toast.LENGTH_SHORT).show()
                                viewModel.clearAddPlaylistState()
                            }
                            is UiState.Error -> {
                                binding.progressAdd.isVisible = false
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                                viewModel.clearAddPlaylistState()
                            }
                            else -> binding.progressAdd.isVisible = false
                        }
                    }
                }

                launch {
                    viewModel.refreshState.collect { state ->
                        when (state) {
                            is UiState.Success ->
                                Toast.makeText(requireContext(), "Updated ${state.data} channels", Toast.LENGTH_SHORT).show()
                            is UiState.Error ->
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
