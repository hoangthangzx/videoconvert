package com.kan.dev.st_042_video_to_mp3.ui.file_convert_to_mp3

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kan.dev.st_042_video_to_mp3.databinding.ItemConvertFileBinding
import com.kan.dev.st_042_video_to_mp3.model.VideoInfo
import com.kan.dev.st_042_video_to_mp3.utils.onSingleClick

class FileConvertAdapter( var context: Context) : RecyclerView.Adapter<FileConvertAdapter.ViewHolder>() {
    lateinit var mListener : onClickItemListener
    var data = listOf<VideoInfo>()
    fun getData(mData: List<VideoInfo>){
        data = mData
        notifyDataSetChanged()
    }

    interface onClickItemListener {
        fun onItemClick(position: Int)
    }

    fun onClickListener (onClickItemListener: onClickItemListener){
        mListener = onClickItemListener
    }

    inner class ViewHolder (val binding: ItemConvertFileBinding): RecyclerView.ViewHolder(binding.root){
        @SuppressLint("SetTextI18n")
        fun bind(position: Int, holder: ViewHolder, mListener: onClickItemListener) {
            binding.tvSize.text = "${data[position].sizeInMB} MB"
            binding.tvDurationVideo.text = data[position].duration
            binding.tvTitle.text = data[position].name
            binding.tvTitle.isSelected = true
            Glide.with(context)
                .asBitmap()
                .load(data[position].uri)
                .into(binding.imvVideoFile)

            binding.imvDelete.onSingleClick {
                mListener.onItemClick(position)
            }
        }
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FileConvertAdapter.ViewHolder {
        val binding = ItemConvertFileBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FileConvertAdapter.ViewHolder, position: Int) {
        holder.bind(position,holder,mListener)
    }

    override fun getItemCount(): Int = data.size
}