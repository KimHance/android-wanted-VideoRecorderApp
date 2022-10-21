package com.preonboarding.videorecorder.domain.mapper

import com.preonboarding.videorecorder.data.entity.RemoteVideo
import com.preonboarding.videorecorder.domain.model.Video

/**
 * android-wanted-VideoRecorderApp
 * @author jaesung
 * @created 2022/10/21
 * @desc
 */

fun List<RemoteVideo>.mapToVideo(): List<Video> {
    val videoList = mutableListOf<Video>()

    this.map {
        videoList.add(
            Video(
                date = it.videoTimeStamp,
                title = it.videoName,
                videoUrl = it.downloadUrl
            )
        )
    }

    return videoList
}