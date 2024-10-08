package com.kan.dev.st_042_video_to_mp3.ui.merger_audio

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kan.dev.st_042_video_to_mp3.databinding.ItemAudioMergerBinding
import com.kan.dev.st_042_video_to_mp3.databinding.ItemConvertFileBinding
import com.kan.dev.st_042_video_to_mp3.model.AudioInfo
import com.kan.dev.st_042_video_to_mp3.model.VideoInfo
import com.kan.dev.st_042_video_to_mp3.ui.file_convert_to_mp3.FileConvertAdapter
import com.kan.dev.st_042_video_to_mp3.utils.onSingleClick

class MergerAudioAdapter ( var context: Context) : RecyclerView.Adapter<MergerAudioAdapter.ViewHolder>() {
    lateinit var mListener : onClickItemListener
    var data = listOf<AudioInfo>()
    fun getData(mData: List<AudioInfo>){
        data = mData
        notifyDataSetChanged()
    }

    interface onClickItemListener {
        fun onItemClick(position: Int)
    }

    fun onClickListener (onClickItemListener: onClickItemListener){
        mListener = onClickItemListener
    }

    inner class ViewHolder (val binding: ItemAudioMergerBinding): RecyclerView.ViewHolder(binding.root){
        @SuppressLint("SetTextI18n")
        fun bind(position: Int, holder: ViewHolder, mListener: onClickItemListener) {
            binding.tvSize.text = "${data[position].sizeInMB} MB"
            binding.tvDuration.text = data[position].duration
            binding.tvTitle.text = data[position].name
            binding.tvTitle.isSelected = true
            binding.imvDelete.onSingleClick {
                mListener.onItemClick(position)
            }
        }
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MergerAudioAdapter.ViewHolder {
        val binding = ItemAudioMergerBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MergerAudioAdapter.ViewHolder, position: Int) {
        holder.bind(position,holder,mListener)
    }

    override fun getItemCount(): Int = data.size
}