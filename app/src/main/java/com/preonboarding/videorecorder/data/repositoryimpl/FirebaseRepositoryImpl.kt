package com.preonboarding.videorecorder.data.repositoryimpl

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.preonboarding.videorecorder.data.datasource.FirebaseDataSource
import com.preonboarding.videorecorder.domain.model.Video
import com.preonboarding.videorecorder.domain.repository.FirebaseRepository
import java.io.File
import javax.inject.Inject

class FirebaseRepositoryImpl @Inject constructor(
    private val firebaseDataSource: FirebaseDataSource,
    private val firebaseStorage: FirebaseStorage
) : FirebaseRepository {
    var ref = firebaseStorage.reference

    override suspend fun getVideoList() {
        //TODO
    }

    override suspend fun uploadVideo(video: Video) {
        Log.d("UPLOAD", video.uri)
        ref.child("test").putFile(
            Uri.fromFile(File(video.uri))
        ).addOnSuccessListener {
            Log.d("UPLOAD SUCCESS", "uploadVideo: ${video.uri}")
        }.addOnFailureListener {
            Log.d("UPLOAD FAIL", it.message.toString())
        }
    }

    override suspend fun deleteVideo(video: Video) {
        //TODO
    }
}