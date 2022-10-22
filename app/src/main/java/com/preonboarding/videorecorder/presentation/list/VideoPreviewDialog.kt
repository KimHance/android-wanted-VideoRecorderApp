package com.preonboarding.videorecorder.presentation.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.preonboarding.videorecorder.databinding.DialogVideoPreviewBinding
import dagger.hilt.android.AndroidEntryPoint
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

    private fun setPreviewExoPlayer(previewUrl: String) {
        val mediaItem = MediaItem.fromUri(previewUrl)

        exoPlayer = ExoPlayer.Builder(requireContext()).build().apply {
            setMediaItem(mediaItem)
            prepare()
            play()
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
    }

    override fun onDestroyView() {
        super.onDestroyView()

        releasePlayer()
    }
}