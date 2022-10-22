package com.preonboarding.videorecorder.presentation.play

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.util.Util
import com.preonboarding.videorecorder.R
import com.preonboarding.videorecorder.databinding.FragmentPlayBinding
import com.preonboarding.videorecorder.presentation.MainViewModel
import com.preonboarding.videorecorder.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PlayFragment : BaseFragment<FragmentPlayBinding>(R.layout.fragment_play) {
    private val playVideoModel: MainViewModel by activityViewModels()

    private val navArgs: PlayFragmentArgs by navArgs()
    private var uri: String = ""

    private var exoPlayer: ExoPlayer? = null
    private var exoPlayWhenReady = true
    private var exoCurrentWindow = 0
    private var exoPlaybackPosition = 0L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fragment = this@PlayFragment
        getSelectedVideo()
    }

    private fun setExoPlayer() {
        val mediaItem = MediaItem.fromUri(uri)
        exoPlayer = ExoPlayer.Builder(requireContext()).build().also {
            binding.pvPlayVideo.player = it
            it.setMediaItem(mediaItem)
            it.playWhenReady = exoPlayWhenReady
            it.seekTo(exoCurrentWindow, exoPlaybackPosition)
            it.prepare()
        }
    }

    private fun collectFlow() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                playVideoModel.selectedVideo.collect { video ->
                    uri = video.videoUrl
                    setExoPlayer()
                }
            }
        }
    }

    private fun getSelectedVideo() {
        playVideoModel.setSelectedVideo(navArgs.video)
        collectFlow()
    }

    fun setBackButton(view: View) {
        requireView().findNavController().popBackStack()
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

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT < 24) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT >= 24) {
            releasePlayer()
        }
    }
}