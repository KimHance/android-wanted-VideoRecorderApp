package com.preonboarding.videorecorder.presentation.list.adapter

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

object ListBindingAdapter {

    @BindingAdapter("videoThumbnail")
    @JvmStatic
    fun setVideoThumbnail(view: ImageView, videoUrl: String?) {
        if (!videoUrl.isNullOrEmpty()) {
            Glide.with(view)
                .asBitmap()
                .load(videoUrl)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(view)
        }
    }
}