package com.kan.dev.st_042_video_to_mp3.ui.select_video

import android.content.Context

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kan.dev.st_042_video_to_mp3.R
import com.kan.dev.st_042_video_to_mp3.databinding.ItemVideoBinding
import com.kan.dev.st_042_video_to_mp3.model.VideoInfo
import com.kan.dev.st_042_video_to_mp3.utils.onSingleClick

class SelectVideoAdapter (var context: Context): RecyclerView.Adapter<SelectVideoAdapter.ViewHolder>() {
    lateinit var mListener : onClickItemListener
    var data = listOf<VideoInfo>()

    fun updateData(newData: List<VideoInfo>) {
        data = newData
        notifyDataSetChanged() // Thông báo cho RecyclerView cập nhật lại giao diện
    }


    fun getData(maData : List<VideoInfo>){
        data = maData
    }
    interface onClickItemListener {
        fun onItemClick(position: Int, holder: ViewHolder)
    }
    fun onClickListener (onClickItemListener: onClickItemListener){
        mListener = onClickItemListener
    }
    inner class ViewHolder(val binding: ItemVideoBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(position: Int, mListener: onClickItemListener, holder: ViewHolder){
            binding.tvExoPlayer.text = data[position].duration
            Glide.with(context)
                .asBitmap()
                .load(data[position].uri)
                .into(binding.imvVideo)
            if(!data[position].active ){
                binding.imvCheckbox.setImageResource(R.drawable.icon_check_box)
            }else{
                binding.imvCheckbox.setImageResource(R.drawable.icon_check_box_yes)
            }
            holder.binding.imvVideo.onSingleClick {
                mListener.onItemClick(position,holder)
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var binding =
            ItemVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position,mListener,holder)
    }
    override fun getItemCount(): Int = data.size
}