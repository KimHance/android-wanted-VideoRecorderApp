package com.preonboarding.videorecorder.data.repositoryimpl

import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.StorageMetadata
import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.preonboarding.videorecorder.data.datasource.FirebaseDataSource
import com.preonboarding.videorecorder.data.entity.RemoteVideo
import com.preonboarding.videorecorder.domain.model.Video
import com.preonboarding.videorecorder.domain.repository.FirebaseRepository
import kotlinx.coroutines.coroutineScope
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class FirebaseRepositoryImpl @Inject constructor(
    private val firebaseDataSource: FirebaseDataSource,
    private val firebaseStorage: FirebaseStorage
) : FirebaseRepository {
    var ref = firebaseStorage.reference

    override suspend fun getVideoList() {
        val videoList = mutableListOf<RemoteVideo>()

        firebaseDataSource.getVideoList().addOnSuccessListener { result ->
            Timber.e("${result.items.size}")
            Timber.e("${result.items}")
            result.items.forEach { reference ->
                videoList.add(
                    RemoteVideo(
                        videoName = reference.name,
                        downloadUrl = getDownloadUrl(reference.downloadUrl),
                        videoTimeStamp = getTimeMillis(reference.metadata)
                    )
                )
            }

            Timber.e("$videoList")
        }
    }

    override suspend fun uploadVideo(video: Video) {
        ref.child("test").child(video.date).putFile(
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