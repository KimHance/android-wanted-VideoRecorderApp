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
import com.preonboarding.videorecorder.domain.model.Video
import com.preonboarding.videorecorder.presentation.MainViewModel
import com.preonboarding.videorecorder.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class PlayFragment : BaseFragment<FragmentPlayBinding>(R.layout.fragment_play) {
    private val playVideoModel: MainViewModel by activityViewModels()

    //TODO 영화목록 연결할 때 아래 주석 해제하기
    private val navArgs: PlayFragmentArgs by navArgs()
    private var uri: String = ""

    //TODO 영화목록 연결할 때 테스트용인 아래 세문장 지우기
//    private val testUri =
//        "https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4"
//    private val testVideo = Video("", testUri)
//    private var uri: String = testUri

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
        Timber.e(uri)
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
//                    uri = video.uri
                    uri = video.videoUrl
                    Timber.e(uri)
                }
            }
        }
    }

    private fun getSelectedVideo() {
        //TODO 영화목록 연결 시 아래 주석해제하고 테스트용 문장 지우기
        Timber.e("${navArgs.video}")
        playVideoModel.setSelectedVideo(navArgs.video)
//        playVideoModel.setSelectedVideo(testVideo) //테스트용
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

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT >= 24) {
            setExoPlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        if ((Util.SDK_INT < 24 || exoPlayer == null)) {
            setExoPlayer()
        }
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