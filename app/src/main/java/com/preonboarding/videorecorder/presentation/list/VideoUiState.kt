package com.preonboarding.videorecorder.presentation.list

/**
 * android-wanted-VideoRecorderApp
 * @author jaesung
 * @created 2022/10/21
 * @desc
 */
sealed class VideoUiState<out T : Any> {
    object Loading : VideoUiState<Nothing>()
    data class Success<out T : Any>(val data: T) : VideoUiState<T>()
}
