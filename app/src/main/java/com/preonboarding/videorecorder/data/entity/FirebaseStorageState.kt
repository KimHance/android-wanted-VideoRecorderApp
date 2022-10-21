package com.preonboarding.videorecorder.data.entity

/**
 * android-wanted-VideoRecorderApp
 * @author jaesung
 * @created 2022/10/21
 * @desc
 */
sealed class FirebaseStorageState<out T> {
    object Loading : FirebaseStorageState<Nothing>()
    data class Success<out T>(val data: T) : FirebaseStorageState<T>()
}
