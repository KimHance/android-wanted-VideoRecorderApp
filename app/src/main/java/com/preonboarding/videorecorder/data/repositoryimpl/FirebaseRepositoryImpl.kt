package com.preonboarding.videorecorder.data.repositoryimpl

import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.storage.FirebaseStorage
import com.preonboarding.videorecorder.data.datasource.FirebaseDataSource
import com.preonboarding.videorecorder.di.DispatcherModule
import com.preonboarding.videorecorder.domain.model.Video
import com.preonboarding.videorecorder.domain.repository.FirebaseRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class FirebaseRepositoryImpl @Inject constructor(
    private val firebaseDataSource: FirebaseDataSource,
    private val firebaseStorage: FirebaseStorage,
    @DispatcherModule.DispatcherIO private val dispatcherIO: CoroutineDispatcher
) : FirebaseRepository {
    var ref = firebaseStorage.reference

    //    private val list = mutableListOf<RemoteVideo>()
    override suspend fun getVideoList() = callbackFlow {
        firebaseDataSource.getVideoList().map { reference ->
            val downloadUrl = reference.downloadUrl
            val getMetadata = reference.metadata
            Tasks.whenAll(
                downloadUrl,
                getMetadata
            ).addOnSuccessListener {
                trySend(
                    Video(
                        date = getMetadata.result.creationTimeMillis.toString(),
                        title = reference.name,
                        videoUrl = downloadUrl.result.toString()
                    )
                )
            }
        }
        awaitClose()
    }.flowOn(dispatcherIO)

    override suspend fun uploadVideo(video: Video) {
        ref.child("test").child(video.date).putFile(
            Uri.fromFile(File(video.videoUrl))
        ).addOnSuccessListener {
            Log.d("UPLOAD SUCCESS", "uploadVideo: ${video.uri}")
        }.addOnFailureListener {
            Log.d("UPLOAD FAIL", it.message.toString())
        }
    }

    override suspend fun deleteVideo(video: Video) {
        firebaseDataSource.deleteVideo(video).addOnSuccessListener {
            Timber.e("Success")
        }.addOnFailureListener {
            it.printStackTrace()
        }
    }
}