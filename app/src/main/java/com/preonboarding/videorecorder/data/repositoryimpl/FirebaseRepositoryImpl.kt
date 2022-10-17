package com.preonboarding.videorecorder.data.repositoryimpl

import com.preonboarding.videorecorder.data.datasource.FirebaseDataSource
import com.preonboarding.videorecorder.domain.model.Video
import com.preonboarding.videorecorder.domain.repository.FirebaseRepository
import javax.inject.Inject

class FirebaseRepositoryImpl @Inject constructor(
    private val firebaseDataSource: FirebaseDataSource
) : FirebaseRepository {
    override suspend fun getVideoList() {
        //TODO
    }

    override suspend fun uploadVideo(video: Video) {
        //TODO
    }

    override suspend fun deleteVideo(video: Video) {
        //TODO
    }
}