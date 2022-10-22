package com.preonboarding.videorecorder.data.datasource

import com.google.firebase.storage.StorageReference
import com.preonboarding.videorecorder.domain.model.Video

interface FirebaseDataSource {

    suspend fun getVideoList(): MutableList<StorageReference>

    suspend fun uploadVideo(video: Video)

    suspend fun deleteVideo(video: Video)
}