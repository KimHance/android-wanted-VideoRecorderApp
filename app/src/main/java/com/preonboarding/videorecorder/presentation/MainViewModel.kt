package com.preonboarding.videorecorder.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.preonboarding.videorecorder.domain.model.Video
import com.preonboarding.videorecorder.domain.usecase.DeleteVideoUseCase
import com.preonboarding.videorecorder.domain.usecase.GetVideoListUseCase
import com.preonboarding.videorecorder.domain.usecase.UploadVideoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getVideoListUseCase: GetVideoListUseCase,
    private val uploadVideoUseCase: UploadVideoUseCase,
    private val deleteVideoUseCase: DeleteVideoUseCase
) : ViewModel() {

    private val _videoList = MutableStateFlow<List<Video>>(emptyList())
    val videoList = _videoList.asStateFlow()

    private val mutableVideoList = mutableListOf<Video>()


    private val _selectedVideo = MutableStateFlow<Video>(Video())
    val selectedVideo = _selectedVideo.asStateFlow()

    fun setSelectedVideo(video: Video) {
        viewModelScope.launch {
            _selectedVideo.value = video
        }
    }

    fun getVideo() {
        viewModelScope.launch {
            val job = async { getVideoListUseCase.invoke().collect {
                it.map { remoteVideo ->
                    mutableVideoList.add(
                        Video(
                            date = remoteVideo.videoTimeStamp,
                            title = remoteVideo.videoName,
                            videoUrl = remoteVideo.downloadUrl
                        )
                    )

                    Timber.e("$mutableVideoList")
                }
            } }

            job.await()
            Timber.e("asdfasdf $mutableVideoList")

            _videoList.value = mutableVideoList
        }
    }

    fun uploadVideo(video: Video) {
        viewModelScope.launch {
            runCatching {
                uploadVideoUseCase(video)
            }.onSuccess {
                //Log.d("Upload", "uploadVideo: SUCCES")
            }.onFailure {
                //Log.d("Upload", "uploadVideo: FAIL")
            }
        }
    }

}