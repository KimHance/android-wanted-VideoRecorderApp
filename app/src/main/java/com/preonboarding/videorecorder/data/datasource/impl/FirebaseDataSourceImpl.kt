package com.preonboarding.videorecorder.data.datasource.impl

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ListResult
import com.google.firebase.storage.StorageReference
import com.preonboarding.videorecorder.data.datasource.FirebaseDataSource
import com.preonboarding.videorecorder.domain.model.Video
import timber.log.Timber
import javax.inject.Inject


class FirebaseDataSourceImpl @Inject constructor(
    firebaseStorage: FirebaseStorage
) : FirebaseDataSource {

    private val firebaseRef = firebaseStorage.reference
    private val videoDirRef = firebaseRef.child("video")

    override suspend fun getVideoList(): MutableList<StorageReference> {
        return Tasks.await(videoDirRef.listAll()).items
    }

    override suspend fun uploadVideo(video: Video): StorageReference {
        return videoDirRef.child(video.date)
    }

    override suspend fun deleteVideo(video: Video): Task<Void> {
        val deleteRef = videoDirRef.child(video.title)
        return deleteRef.delete()
    }
}