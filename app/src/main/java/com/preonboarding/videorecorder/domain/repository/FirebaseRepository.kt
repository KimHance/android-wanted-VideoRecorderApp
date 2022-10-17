package com.preonboarding.videorecorder.domain.repository

import com.preonboarding.videorecorder.domain.model.Video

interface FirebaseRepository {
    suspend fun getVideoList()

    suspend fun uploadVideo(video: Video)

    suspend fun deleteVideo(video: Video)
}