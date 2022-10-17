package com.preonboarding.videorecorder.data.datasource.impl

import com.google.firebase.storage.FirebaseStorage
import com.preonboarding.videorecorder.data.datasource.FirebaseDataSource
import com.preonboarding.videorecorder.domain.model.Video
import javax.inject.Inject


class FirebaseDataSourceImpl @Inject constructor(
    private val firebaseStorage: FirebaseStorage
) : FirebaseDataSource {

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