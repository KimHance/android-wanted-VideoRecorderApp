package com.preonboarding.videorecorder.presentation.play

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.preonboarding.videorecorder.R
import com.preonboarding.videorecorder.databinding.FragmentPlayBinding
import com.preonboarding.videorecorder.presentation.MainViewModel
import com.preonboarding.videorecorder.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlayFragment : BaseFragment<FragmentPlayBinding>(R.layout.fragment_play) {
    private val playVideoModel: MainViewModel by activityViewModels()
    private val navArgs: PlayFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 선택된 비디오 세팅
        //getSelectedVideo()
        collectFlow()
        initView()
    }

    private fun initView() {
        binding.apply {
            // TODO 네비게이션 설정 되면 붙히세요
            // 안그러면 터집니다.
            /*ivPlayBack.setOnClickListener {
                requireView().findNavController().popBackStack()
            }*/
        }
    }

    private fun collectFlow() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                playVideoModel.selectedVideo.collect { video ->
                    // TODO 선택된 비디오 ExoPlayer와 작업 필요
                }
            }
        }
    }

    private fun getSelectedVideo() {
        playVideoModel.setSelectedVideo(navArgs.video)
    }
}