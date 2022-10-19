package com.preonboarding.videorecorder.presentation.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.preonboarding.videorecorder.databinding.ItemVideoInfoBinding
import com.preonboarding.videorecorder.domain.model.Video

/**
 * android-wanted-VideoRecorderApp
 * @author jaesung
 * @created 2022/10/19
 * @desc
 */
class VideoAdapter(
    private val onItemClicked: (Video) -> Unit,
    private val onItemLongClicked: (String) -> Unit
) : ListAdapter<Video, VideoAdapter.VideoViewHolder>(VIDEO_DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemVideoInfoBinding.inflate(inflater, parent, false)
        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bindItems(getItem(position), onItemClicked, onItemLongClicked)
    }

    class VideoViewHolder(private val binding: ItemVideoInfoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindItems(
            item: Video,
            onItemClicked: (Video) -> Unit,
            onItemLongClicked: (String) -> Unit
        ) {
            with(binding) {
                video = item
                executePendingBindings()

                cvVideo.setOnLongClickListener {
                    onItemLongClicked.invoke(item.videoUrl)
                    return@setOnLongClickListener true // Long Click 이후 Click 발생 방지
                }

                cvVideo.setOnClickListener {
                    onItemClicked.invoke(item)
                }
            }
        }
    }

    companion object {
        private val VIDEO_DIFF_CALLBACK = object : DiffUtil.ItemCallback<Video>() {
            override fun areItemsTheSame(oldItem: Video, newItem: Video): Boolean {
                return oldItem.hashCode() == newItem.hashCode()  // 수정 필요
            }

            override fun areContentsTheSame(oldItem: Video, newItem: Video): Boolean {
                return oldItem == newItem
            }
        }
    }
}