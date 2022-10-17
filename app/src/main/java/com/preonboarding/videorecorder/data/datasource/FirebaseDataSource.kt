package com.preonboarding.videorecorder.data.datasource

import com.preonboarding.videorecorder.domain.model.Video

interface FirebaseDataSource {

    suspend fun getVideoList()

    suspend fun uploadVideo(video: Video)

    suspend fun deleteVideo(video: Video)
}