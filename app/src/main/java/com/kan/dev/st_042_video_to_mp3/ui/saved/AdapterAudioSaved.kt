package com.kan.dev.st_042_video_to_mp3.ui.saved

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kan.dev.st_042_video_to_mp3.databinding.ItemSaveConvertedFileBinding
import com.kan.dev.st_042_video_to_mp3.model.AudioSpeedModel
import com.kan.dev.st_042_video_to_mp3.utils.Const.listVideoPick
import com.kan.dev.st_042_video_to_mp3.utils.onSingleClick

class AdapterAudioSaved (var context: Context): RecyclerView.Adapter<AdapterAudioSaved.ViewHolder>() {
    lateinit var mListener : onClickItemListener
    var data = listOf<AudioSpeedModel>()
    fun getData(mData : List<AudioSpeedModel>){
        data = mData
        notifyDataSetChanged()
    }

    interface onClickItemListener {
        fun onClickItem (position: Int, holder: ViewHolder)
    }

    fun onClickListener(onClickItemListener: onClickItemListener){
        mListener = onClickItemListener
    }

    inner class ViewHolder(val binding : ItemSaveConvertedFileBinding) : RecyclerView.ViewHolder(binding.root){
        @SuppressLint("SetTextI18n")
        fun bind(position: Int, holder: AdapterAudioSaved.ViewHolder) {
            binding.tvTitle.text = data[position].name
            binding.tvSize.text = "${data[position].sizeInMB}"
            binding.tvDurationVideo.text = data[position].duration
            binding.tvTitle.isSelected = true

            holder.itemView.onSingleClick {
                mListener.onClickItem(position,holder)
            }
        }
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AdapterAudioSaved.ViewHolder {
        val binding = ItemSaveConvertedFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return  ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AdapterAudioSaved.ViewHolder, position: Int) {
        holder.bind(position,holder)
    }

    override fun getItemCount(): Int = data.size

    fun formatTimeToHoursMinutes(duration: Long): String {
        val minutes = (duration / 1000) / 60
        val seconds = (duration / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

}