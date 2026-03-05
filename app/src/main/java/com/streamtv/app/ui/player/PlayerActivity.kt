package com.streamtv.app.ui.player

import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.streamtv.app.data.model.Channel
import com.streamtv.app.databinding.ActivityPlayerBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private val viewModel: PlayerViewModel by viewModels()

    private var player: ExoPlayer? = null
    private var isPlayerReady = false
    private var channel: Channel? = null

    companion object {
        private const val EXTRA_CHANNEL = "extra_channel"

        fun start(context: Context, channel: Channel) {
            context.startActivity(
                Intent(context, PlayerActivity::class.java).apply {
                    putExtra(EXTRA_CHANNEL, channel)
                }
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowFlags()
        channel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_CHANNEL, Channel::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_CHANNEL)
        }

        if (channel == null) {
            Toast.makeText(this, "Channel not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupUI()
        initializePlayer()
        observeViewModel()
    }

    private fun setupWindowFlags() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        hideSystemUI()
    }

    private fun hideSystemUI() {
        WindowInsetsControllerCompat(window, binding.root).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun setupUI() {
        binding.apply {
            tvChannelName.text = channel?.name
            tvChannelGroup.text = channel?.group

            btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
            btnPip.setOnClickListener { enterPictureInPicture() }
            btnFavorite.setOnClickListener {
                channel?.let { viewModel.toggleFavorite(it) }
            }
        }
    }

    private fun initializePlayer() {
        player = ExoPlayer.Builder(this).build().also { exo ->
            binding.playerView.player = exo
            binding.playerView.useController = true

            val mediaItem = MediaItem.fromUri(channel!!.url)
            exo.setMediaItem(mediaItem)
            exo.prepare()
            exo.playWhenReady = true

            exo.addListener(object : Player.Listener {
                override fun onIsLoadingChanged(isLoading: Boolean) {
                    binding.progressLoading.isVisible = isLoading
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_READY -> {
                            isPlayerReady = true
                            binding.progressLoading.isVisible = false
                            binding.tvError.isVisible = false
                        }
                        Player.STATE_BUFFERING -> {
                            binding.progressLoading.isVisible = true
                        }
                        Player.STATE_ENDED -> {
                            // Live streams shouldn't end; attempt reconnect
                            exo.prepare()
                        }
                        Player.STATE_IDLE -> {}
                    }
                }

                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    binding.progressLoading.isVisible = false
                    binding.tvError.isVisible = true
                    binding.tvError.text = "Playback error: ${error.message}"
                }
            })
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isFavorite.collect { isFavorite ->
                    binding.btnFavorite.setImageResource(
                        if (isFavorite) com.streamtv.app.R.drawable.ic_favorite_filled
                        else com.streamtv.app.R.drawable.ic_favorite_outline
                    )
                }
            }
        }

        channel?.let { viewModel.init(it) }
    }

    private fun enterPictureInPicture() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(16, 9))
                .build()
            enterPictureInPictureMode(params)
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        binding.controlsContainer.isVisible = !isInPictureInPictureMode
    }

    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            player?.play()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !isInPictureInPictureMode) {
            player?.pause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }
}
