package com.preonboarding.videorecorder.presentation.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.preonboarding.videorecorder.databinding.DialogVideoPreviewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * android-wanted-VideoRecorderApp
 * @author jaesung
 * @created 2022/10/22
 * @desc
 */
@AndroidEntryPoint
class VideoPreviewDialog : DialogFragment() {

    private lateinit var binding: DialogVideoPreviewBinding

    private var exoPlayer: ExoPlayer? = null
    private var exoPlayWhenReady = true
    private var exoCurrentWindow = 0
    private var exoPlaybackPosition = 0L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogVideoPreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val previewUrl = requireArguments().getString("VIDEO_URL").toString()
        Timber.e(previewUrl)

        setPreviewExoPlayer(previewUrl)
    }

    private fun exoPreviewListener() = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)

            when (playbackState) {
                ExoPlayer.STATE_IDLE -> Timber.e("STATE_IDLE")
                ExoPlayer.STATE_BUFFERING -> Timber.e("STATE_BUFFERING")
                ExoPlayer.STATE_READY -> {
                    Timber.e("STATE_READY")
                    Timber.e("video duration : ${exoPlayer?.duration}")
                }

                ExoPlayer.STATE_ENDED -> Timber.e("STATE_ENDED")

            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)

            if (isPlaying) {
                viewLifecycleOwner.lifecycleScope.launch {
                    delay(5000L)
                    releasePlayer()
                    dialog?.dismiss()
                }
            } else {
                if (exoPlayer?.currentPosition!! >= 5000) {
                    dialog?.dismiss()
                }
            }
        }
    }

    private fun setPreviewExoPlayer(previewUrl: String) {
        val mediaItem = MediaItem.fromUri(previewUrl)

        val previewListener = exoPreviewListener()

        exoPlayer = ExoPlayer.Builder(requireContext()).build().also { player ->
            player.setMediaItem(mediaItem)
            player.addListener(previewListener)
            player.playWhenReady = exoPlayWhenReady
            player.seekTo(exoCurrentWindow, exoPlaybackPosition)
            player.prepare()
        }

        binding.pvVideoPreview.player = exoPlayer
    }

    private fun releasePlayer() {
        exoPlayer?.run {
            exoPlaybackPosition = this.currentPosition
            exoCurrentWindow = this.currentMediaItemIndex
            exoPlayWhenReady = this.playWhenReady
            release()
        }

        exoPlayer = null
    }

    override fun onDestroyView() {
        super.onDestroyView()

        releasePlayer()
    }
}