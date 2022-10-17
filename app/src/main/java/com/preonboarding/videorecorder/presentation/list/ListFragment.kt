package com.preonboarding.videorecorder.presentation.list

import androidx.fragment.app.activityViewModels
import com.preonboarding.videorecorder.R
import com.preonboarding.videorecorder.databinding.FragmentListBinding
import com.preonboarding.videorecorder.presentation.MainViewModel
import com.preonboarding.videorecorder.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ListFragment : BaseFragment<FragmentListBinding>(R.layout.fragment_list) {
    private val listViewModel: MainViewModel by activityViewModels()
}