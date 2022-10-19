package com.preonboarding.videorecorder.data.repositoryimpl

import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.StorageMetadata
import com.preonboarding.videorecorder.data.datasource.FirebaseDataSource
import com.preonboarding.videorecorder.data.entity.RemoteVideo
import com.preonboarding.videorecorder.domain.model.Video
import com.preonboarding.videorecorder.domain.repository.FirebaseRepository
import kotlinx.coroutines.coroutineScope
import timber.log.Timber
import javax.inject.Inject

class FirebaseRepositoryImpl @Inject constructor(
    private val firebaseDataSource: FirebaseDataSource
) : FirebaseRepository {
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
        //TODO
    }

    override suspend fun deleteVideo(video: Video) {
        //TODO
    }

    private fun getTimeMillis(metadata: Task<StorageMetadata>): String {
        var timeMillis = ""
        metadata.addOnSuccessListener { storageMetaData ->
            Timber.e(timeMillis)
            timeMillis = storageMetaData.creationTimeMillis.toString()
        }
        return timeMillis
    }

    private fun getDownloadUrl(downloadUrl: Task<Uri>): String {
        var url = ""
        downloadUrl.addOnSuccessListener {
            Timber.e("$it")
            url = it.toString()
        }.addOnFailureListener {
            Timber.e("${it.message}")
        }
        Timber.e(url)
        return url
    }

    suspend fun getDownloadUrl() = coroutineScope {
        firebaseDataSource.getVideoList().addOnSuccessListener {
            val reference = it.items
        }
    }
}