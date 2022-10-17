package com.preonboarding.videorecorder.presentation.play

import androidx.fragment.app.activityViewModels
import com.preonboarding.videorecorder.R
import com.preonboarding.videorecorder.databinding.FragmentPlayBinding
import com.preonboarding.videorecorder.presentation.MainViewModel
import com.preonboarding.videorecorder.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlayFragment : BaseFragment<FragmentPlayBinding>(R.layout.fragment_play) {
    private val playVideModel: MainViewModel by activityViewModels()
}