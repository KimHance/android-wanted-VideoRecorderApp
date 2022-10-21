package com.preonboarding.videorecorder.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Video(
    val date: String = "",
    val title: String = "",
    val videoUrl: String = "",
    val uri: String = ""
) : Parcelable
