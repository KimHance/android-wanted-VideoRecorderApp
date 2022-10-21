package com.preonboarding.videorecorder.data.repositoryimpl

import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.storage.FirebaseStorage
import com.preonboarding.videorecorder.data.datasource.FirebaseDataSource
import com.preonboarding.videorecorder.data.entity.FirebaseStorageState
import com.preonboarding.videorecorder.data.entity.RemoteVideo
import com.preonboarding.videorecorder.di.DispatcherModule
import com.preonboarding.videorecorder.domain.mapper.mapToVideo
import com.preonboarding.videorecorder.domain.model.Video
import com.preonboarding.videorecorder.domain.repository.FirebaseRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import javax.inject.Inject

class FirebaseRepositoryImpl @Inject constructor(
    private val firebaseDataSource: FirebaseDataSource,
    private val firebaseStorage: FirebaseStorage,
    @DispatcherModule.DispatcherIO private val dispatcherIO: CoroutineDispatcher
) : FirebaseRepository {
    var ref = firebaseStorage.reference
    override suspend fun getVideoList(): Flow<List<RemoteVideo>> {
        TODO("Not yet implemented")
    }

//    private val list = mutableListOf<RemoteVideo>()
//    override suspend fun getVideoList() = callbackFlow {
//        firebaseDataSource.getVideoList()?.items?.forEach { reference ->
//            val downloadUrl = reference.downloadUrl
//            val getMetadata = reference.metadata
//            Tasks.whenAll(
//                downloadUrl,
//                getMetadata
//            ).addOnSuccessListener {
//                val list = mutableListOf<RemoteVideo>()
//                list.add(
//                    RemoteVideo(
//                        videoName = reference.name,
//                        videoTimeStamp = getMetadata.result.creationTimeMillis.toString(),
//                        downloadUrl = downloadUrl.result.toString()
//                    )
//                )
//                trySend(list)
//            }
//        }
//        awaitClose()
//    }.flowOn(dispatcherIO)

    override suspend fun getVideo() = flow {
        val videoList = listOf<RemoteVideo>(
            RemoteVideo("AA", "1111", "https://firebasestorage.googleapis.com/v0/b/wanted-videorecorderapp.appspot.com/o/mock%2Fmock_video_14.mp4?alt=media&token=d5f53439-63ea-4008-b7f4-92d13b08334a"),
            RemoteVideo("BB", "1122", "https://firebasestorage.googleapis.com/v0/b/wanted-videorecorderapp.appspot.com/o/mock%2Fmock_netfilx.mp4?alt=media&token=d722543f-f989-4663-acb1-0a6cc81702c2"),
            RemoteVideo("CC", "1133", "https://firebasestorage.googleapis.com/v0/b/wanted-videorecorderapp.appspot.com/o/mock%2Fmock_video_12.mp4?alt=media&token=49778ea6-f745-451c-96c6-4c3361d5630d"),
            RemoteVideo("DD", "1144", "https://firebasestorage.googleapis.com/v0/b/wanted-videorecorderapp.appspot.com/o/mock%2Fmock_video_3.mp4?alt=media&token=d3b2faab-2799-4356-a2f5-61c4c95d3659"),
            RemoteVideo("EE", "1155", "https://firebasestorage.googleapis.com/v0/b/wanted-videorecorderapp.appspot.com/o/mock%2Fmock_video_14.mp4?alt=media&token=d5f53439-63ea-4008-b7f4-92d13b08334a"),
            RemoteVideo("FF", "1166", "https://firebasestorage.googleapis.com/v0/b/wanted-videorecorderapp.appspot.com/o/mock%2Fmock_netfilx.mp4?alt=media&token=d722543f-f989-4663-acb1-0a6cc81702c2"),
            RemoteVideo("GG", "1177", "https://firebasestorage.googleapis.com/v0/b/wanted-videorecorderapp.appspot.com/o/mock%2Fmock_video_12.mp4?alt=media&token=49778ea6-f745-451c-96c6-4c3361d5630d"),
            RemoteVideo("HH", "1188", "https://firebasestorage.googleapis.com/v0/b/wanted-videorecorderapp.appspot.com/o/mock%2Fmock_video_3.mp4?alt=media&token=d3b2faab-2799-4356-a2f5-61c4c95d3659")
        ).mapToVideo()
        emit(FirebaseStorageState.Loading)
        emit(FirebaseStorageState.Success(videoList))
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