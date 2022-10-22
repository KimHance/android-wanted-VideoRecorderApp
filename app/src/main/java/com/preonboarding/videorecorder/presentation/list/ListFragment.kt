package com.preonboarding.videorecorder.presentation.list

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
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

    private val videoAdapter by lazy {
        VideoAdapter(
            onItemClicked = {
                doOnClick(it)
            },
            onItemLongClicked = {
                doOnLongClick(it)
            },
            onItemDeleted = {
                Timber.e("$it")
                deleteItem(it)
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        collectFlow()

        initView()
        listViewModel.getVideoList()
    }

    private fun initView() {
        binding.rvVideoList.adapter = videoAdapter

        binding.fabRecord.setOnClickListener {
            findNavController().navigate(R.id.action_listFragment_to_recordFragment)
        }
    }

    private fun collectFlow() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                listViewModel.videoList.collect { videoList ->
                    videoAdapter.submitList(videoList)
                    Timber.e("$videoList")
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

    private fun doOnLongClick(url: String) {
        val dialogFragment = VideoPreviewDialog()
        dialogFragment.arguments = bundleOf("VIDEO_URL" to url)
        dialogFragment.show(childFragmentManager, null)
    }

    private fun deleteItem(video: Video) {
        listViewModel.deleteVideo(video)
    }
}