package com.preonboarding.videorecorder.domain.repository

import com.google.android.gms.tasks.Task
import com.preonboarding.videorecorder.data.entity.RemoteVideo
import com.preonboarding.videorecorder.domain.model.Video
import kotlinx.coroutines.flow.Flow

interface FirebaseRepository {
    suspend fun getVideoList(): Flow<Video>

    suspend fun uploadVideo(video: Video)

    suspend fun deleteVideo(video: Video)
}