package com.kan.dev.st_042_video_to_mp3.ui.select_audio

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kan.dev.st_042_video_to_mp3.R
import com.kan.dev.st_042_video_to_mp3.databinding.ItemAudioBinding
import com.kan.dev.st_042_video_to_mp3.model.AudioInfo
import com.kan.dev.st_042_video_to_mp3.ui.select_video.SelectVideoAdapter
import com.kan.dev.st_042_video_to_mp3.ui.select_video.SelectVideoAdapter.onClickItemListener
import com.kan.dev.st_042_video_to_mp3.utils.onSingleClick

class SelectAudioAdapter (var context: Context): RecyclerView.Adapter<SelectAudioAdapter.ViewHolder>() {
    lateinit var mListener : onClickItemListener
    var data = listOf<AudioInfo>()
    fun getData(mData : List<AudioInfo>){
        data = mData
        notifyDataSetChanged()
    }

    interface onClickItemListener {
        fun onClickItem (position: Int, holder: ViewHolder)
    }

    fun onClickListener(onClickItemListener: onClickItemListener){
        mListener = onClickItemListener
    }

    inner class ViewHolder(val binding : ItemAudioBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(position: Int, holder: SelectAudioAdapter.ViewHolder) {
            binding.tvTitle.isSelected = true
            binding.tvSize.text = "${data[position].sizeInMB} MB"
            binding.tvTitle.text = data[position].name
            binding.tvTime.text = data[position].date
            binding.tvDurationVideo.text = data[position].duration
            binding.tvTitle.isSelected = true
            if(!data[position].active ){
                binding.imvTick.setImageResource(R.drawable.icon_check_box)
            }else{
                binding.imvTick.setImageResource(R.drawable.icon_check_box_yes)
            }
            binding.imvTick.setOnClickListener {
                mListener.onClickItem(position,holder)
            }
        }
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SelectAudioAdapter.ViewHolder {
        val binding = ItemAudioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return  ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SelectAudioAdapter.ViewHolder, position: Int) {
        holder.bind(position,holder)
    }

    override fun getItemCount(): Int = data.size
}