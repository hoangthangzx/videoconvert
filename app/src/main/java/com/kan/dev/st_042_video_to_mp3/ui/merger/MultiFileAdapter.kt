package com.kan.dev.st_042_video_to_mp3.ui.merger

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.DOWN
import androidx.recyclerview.widget.ItemTouchHelper.END
import androidx.recyclerview.widget.ItemTouchHelper.START
import androidx.recyclerview.widget.ItemTouchHelper.UP
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kan.dev.st_042_video_to_mp3.R
import com.kan.dev.st_042_video_to_mp3.databinding.ItemMergerStyleBinding
import com.kan.dev.st_042_video_to_mp3.model.VideoInfo
import com.kan.dev.st_042_video_to_mp3.utils.onSingleClick
import java.util.Collections

class MultiFileAdapter (var context: Context) : RecyclerView.Adapter<MultiFileAdapter.ViewHolder>() {

    lateinit var mListener : onClickItemListener
    var data = mutableListOf<VideoInfo>()
    fun getData(maData : MutableList<VideoInfo>){
        data = maData
    }

    interface onClickItemListener {
        fun onItemClick(position: Int, holder: MultiFileAdapter.ViewHolder)
    }
    fun onClickListener (onClickItemListener: onClickItemListener){
        mListener = onClickItemListener
    }

    inner class ViewHolder(val binding: ItemMergerStyleBinding): RecyclerView.ViewHolder(binding.root){
        @SuppressLint("SetTextI18n")
        fun bind(position: Int, mListener: onClickItemListener, holder: ViewHolder){
            Glide.with(context)
                .asBitmap()
                .load(data[position].uri)
                .into(binding.imvVideo)

            binding.tvCount.text = (position + 1).toString()
            binding.tvDuration.text = "${data[position].duration} seconds"

            if(data[position].active == false){
                holder.itemView.setBackgroundResource(R.drawable.ng_pick_item_merger)
            }else{
                holder.itemView.setBackgroundResource(R.color.transparent)
            }

            holder.itemView.onSingleClick {
                mListener.onItemClick(position,holder)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MultiFileAdapter.ViewHolder {
        var binding =
            ItemMergerStyleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MultiFileAdapter.ViewHolder, position: Int) {
        holder.bind(position,mListener,holder)
    }

    override fun getItemCount(): Int = data.size



}