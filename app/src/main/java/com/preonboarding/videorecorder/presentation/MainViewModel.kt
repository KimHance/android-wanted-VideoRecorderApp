package com.preonboarding.videorecorder.presentation

import androidx.lifecycle.ViewModel
import com.preonboarding.videorecorder.domain.model.Video
import com.preonboarding.videorecorder.domain.usecase.DeleteVideoUseCase
import com.preonboarding.videorecorder.domain.usecase.GetVideoListUseCase
import com.preonboarding.videorecorder.domain.usecase.UploadVideoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getVideoListUseCase: GetVideoListUseCase,
    private val uploadVideoUseCase: UploadVideoUseCase,
    private val deleteVideoUseCase: DeleteVideoUseCase
) : ViewModel() {

    private val _videoList = MutableStateFlow<List<Video>>(emptyList())
    val videoList = _videoList.asStateFlow()

    private val _selectedVideo = MutableStateFlow<Video>(Video())
    val selectedVideo = _selectedVideo.asStateFlow()

}