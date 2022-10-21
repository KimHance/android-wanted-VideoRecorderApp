package com.preonboarding.videorecorder.domain.repository

import com.preonboarding.videorecorder.domain.model.Video
import kotlinx.coroutines.flow.Flow

interface FirebaseRepository {
    suspend fun getVideoList(): Flow<Video>

    suspend fun uploadVideo(video: Video)

    suspend fun deleteVideo(video: Video)
}