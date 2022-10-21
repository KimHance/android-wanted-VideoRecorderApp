package com.preonboarding.videorecorder.data.datasource

import com.google.android.gms.tasks.Task
import com.google.firebase.storage.ListResult
import com.preonboarding.videorecorder.domain.model.Video

interface FirebaseDataSource {

    suspend fun getVideoList(): ListResult?

    suspend fun uploadVideo(video: Video)

    suspend fun deleteVideo(video: Video)
}