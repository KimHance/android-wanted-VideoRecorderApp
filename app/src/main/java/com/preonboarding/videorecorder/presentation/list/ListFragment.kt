package com.preonboarding.videorecorder.presentation.list

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import com.preonboarding.videorecorder.R
import com.preonboarding.videorecorder.databinding.FragmentListBinding
import com.preonboarding.videorecorder.domain.model.Video
import com.preonboarding.videorecorder.presentation.MainViewModel
import com.preonboarding.videorecorder.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class ListFragment : BaseFragment<FragmentListBinding>(R.layout.fragment_list) {
    private val listViewModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        collectFlow()

        Timber.e("MESSAGE")
    }

    private fun collectFlow() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                listViewModel.videoList.collect { videoList ->
                    // TODO 비디오 리스트 변경시 어댑터 리스트 업데이트 필요
                }
            }
        }
    }

    // 리스트 아이템 클릭시 플레이로 이동하는 람다
    private fun doOnClick(video: Video) {
        val action =
            ListFragmentDirections.actionListFragmentToPlayFragment(
                video
            )
        requireView().findNavController().navigate(action)
    }
}